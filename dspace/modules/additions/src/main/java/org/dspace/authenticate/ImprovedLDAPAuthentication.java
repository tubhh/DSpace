/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authenticate;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

/**
 *
 * This plugin implements LDAPAuthentication, rewritten to better support "bind as self" searching in flat
 * directories structures, and using a simple bean for LDAP results rather than the previous SpeakerToLDAP which
 * performed LDAP requests as well as handled attribute results.
 *
 * @author Kim Shepherd
 * @version $Revision$
 */
public class ImprovedLDAPAuthentication implements AuthenticationMethod {

    // Initialise logger
    private static Logger log = Logger.getLogger(ImprovedLDAPAuthentication.class);

    /**
     * Internal class to handle results. Just a simple bean with getters and setters.
     * Each member object represents an LDAP attribute, except for 'bound' which is true
     * if the search was bound to LDAP as the authenticating user, so that later auth
     * attempts can be skipped.
     */
    private static class LDAPResult {

        private String ldapEmail = null;
        private String ldapGivenName = null;
        private String ldapSurname = null;
        private String ldapPhone = null;
        private List<String> ldapGroups = null;
        private String dn = null;
        private boolean bound = false;

        LDAPResult() {
            ldapGroups = new ArrayList<>();
        }

        public String getLdapEmail() {
            return ldapEmail;
        }

        public void setLdapEmail(String ldapEmail) {
            this.ldapEmail = ldapEmail;
        }

        public String getLdapGivenName() {
            return ldapGivenName;
        }

        public void setLdapGivenName(String ldapGivenName) {
            this.ldapGivenName = ldapGivenName;
        }

        public String getLdapSurname() {
            return ldapSurname;
        }

        public void setLdapSurname(String ldapSurname) {
            this.ldapSurname = ldapSurname;
        }

        public String getLdapPhone() {
            return ldapPhone;
        }

        public void setLdapPhone(String ldapPhone) {
            this.ldapPhone = ldapPhone;
        }

        public List<String> getLdapGroups() {
            return ldapGroups;
        }

        public void setLdapGroups(List<String> ldapGroups) {
            this.ldapGroups = ldapGroups;
        }

        public void addLdapGroup(String ldapGroup) {
            ldapGroups.add(ldapGroup);
        }

        public String getDN() {
            return dn;
        }

        public void setDN(String dn) {
            this.dn = dn;
        }

        public boolean selfBound() {
            return bound;
        }

        public void setBound(boolean bound) {
            this.bound = bound;
        }

        /**
         * Build string representation of the LDAPResult object
         * for logging or display purposes
         * @return all attributes as a single string
         */
        public String toString() {

            StringBuilder sb = new StringBuilder();
            sb.append("LDAP DN: ");
            sb.append(dn);
            sb.append("\nLDAP Email: ");
            sb.append(ldapEmail);
            sb.append("\nLDAP Given Name: ");
            sb.append(ldapGivenName);
            sb.append("\nLDAP Surame: ");
            sb.append(ldapSurname);
            sb.append("\nLDAP Phone: ");
            sb.append(ldapPhone);
            sb.append("\nLDAP Group: ");
            for(String g : ldapGroups) {
                sb.append(g);
            }

            return sb.toString();
        }

    }

    /**
     * Is the user allowed to set password
     */
    @Override
    public boolean allowSetPassword(Context context,
                                    HttpServletRequest request,
                                    String username) throws SQLException {
        // This is not supported (or recommended) in DSpace LDAP, return false
        return false;
    }

    /**
     * LDAP special groups might depend on the ldap search result. Compile a list of group IDs user will be assigned to
     * as special groups. Special groups are groups assigned depending on login values for the reminder of a user's
     * session. Those groups should not be assigned permanently as the values the LDAP server returns might change.
     * This method respects as well the login.specialgroup property as the login.groupmap.* properties.
     * The list of special groups will be stored as session attribute, which gets read by the method getSpecialGroups().
     */
    private void assignGroups(String dn, List<String> groups, Context context, HttpServletRequest request) {
        if (context.getCurrentUser() == null) { log.debug("Won't look for special groups as context.getCurrentUser() returns null"); }
        if (context.getCurrentUser().getNetid() == null) { log.debug("Won't look for special groups as context.getCurrentUser().getNetid() is null"); }
        if (context.getCurrentUser().getNetid().equals("")) { log.debug("Won't look for special groups as context.getCurrentUser().getNetid() is empty"); }
        if (context.getCurrentUser() != null
                && context.getCurrentUser().getNetid() != null
                && !context.getCurrentUser().getNetid().equals("")) {

            // Store the gorup IDs in a list as we do not know yet how many entries we'll find. Convert the list to an
            // int array at the end of this method.
            List<Integer> specialGroups = new ArrayList<>();

            // check special group that shall be assigned to all LDAP users (moved code from getSepcialGroup() here)
            String groupName = ConfigurationManager.getProperty("authentication-ldap",
                    "login.specialgroup");
            String ldapGroupField = ConfigurationManager.getProperty(
                "authentication-ldap", "login.groupmap.attribute");
            
            log.debug("Assinging ldap special group " + groupName);
            
            // If a special group is configured, try to load it from the database to obtain its ID
            if ((groupName != null) && (!groupName.trim().equals(""))) {
                Group ldapGroup = null;
                try {
                    ldapGroup = Group.findByName(context, groupName);
                } catch (SQLException ex) {
                    log.warn("Caught an SQLException, while trying to find ldap special group '"+ groupName
                            + "'", ex);
                }
                if (ldapGroup == null) {
                    // Group not found, return empty group ID array
                    log.warn(LogManager.getHeader(context,
                            "ldap_specialgroup",
                            "Group defined in login.specialgroup does not exist"));
                } else {
                    // add the group's id to the list of special groups
                    specialGroups.add(ldapGroup.getID());
                    log.debug(LogManager.getHeader(context, "ldap_specialgroup", "assigne group "
                            + groupName + " : " + ldapGroup.getID() + "."));
                }
            }


            log.debug("Checking group mapping based on OU or ldap attr");
            // also check authenticatation-ldap.login.groupmap.*
            // initialize while loop to iterater over all properties
            boolean cmp;
            int i = 1;
            // try to load the first configured groupmap
            String groupMap = ConfigurationManager.getProperty("authentication-ldap",
                    "login.groupmap." + i);

            // loop as long as we find groupmaps
            while (groupMap != null) {
                log.debug("looking up ldap groupmap " + groupMap);
                // split the value of our configuration property on a colon
                String groupMapParts[] = groupMap.split("\\s*:\\s*");
                String ldapGroupName = null;
                String dspaceGroupName;
                // check the configuration property
                if (groupMapParts.length > 1) {
                    ldapGroupName = groupMapParts[0];
                    dspaceGroupName = groupMapParts[1];
                } else {
                    // Invalid group mapping configuration value, it should result in a 2-element array
                    // Log error and continue to next group mapping
                    log.error(LogManager.getHeader(context, "Invalid LDAP group mapping value ", groupMap));
                    // Get the next mapping in the list (or set it null, so we'll leave the loop)
                    groupMap = ConfigurationManager.getProperty(
                            "authentication-ldap", "login.groupmap." + (++i));
                    continue;
                }

                for(String group : groups) {
                    if(ldapGroupField != null) {
                        // There is an attribute to check
                        cmp = StringUtils.equalsIgnoreCase(group, ldapGroupName);
                        log.debug("group attribute is not null, comparing it to the ldapGroupName of " + ldapGroupField);

                        if (cmp) {
                            // We have a match, add the group as special group of the current user, so that the current user
                            // is assigned to the mapped DSpace group for the remainder of their current session
                            log.debug("found the group " + ldapGroupName + " configured by an attribute map");
                            // try to load the group within DSpace
                            Group ldapGroup = null;
                            try {
                                ldapGroup = Group.findByName(context, dspaceGroupName);
                            } catch (SQLException ex) {
                                log.warn("Caught an SQLException, while trying to find ldap special group '" + groupName
                                    + "'", ex);
                            }
                            if (ldapGroup != null) {
                                // group does exists, add its ID to the list of special groups
                                specialGroups.add(ldapGroup.getID());
                                log.debug("added group " + dspaceGroupName + " : " + ldapGroup.getID());
                            } else {
                                // The group could not be found. Warn in the logs, but continue without exception.
                                log.warn(LogManager.getHeader(context,
                                    "ldap_assignGroups",
                                    "Group defined in authentication-ldap.login.groupmap." + i +
                                        " does not exist :: " + dspaceGroupName));
                            }
                        }
                    }
                }

                if(ldapGroupField == null && groups.isEmpty()) {
                    // Group attribute is null, check to see if the user's DN contains the LDAP group name instead
                    log.debug("no group attribute configured, checking DN to see if it contains specified group");
                    if(StringUtils.containsIgnoreCase(dn, ldapGroupName + ",")) {
                        // We have a match, add the group as special group of the current user, so that the current user
                        // is assigned to the mapped DSpace group for the remainder of their current session
                        log.debug("found the group " + ldapGroupName + " in the DN of the user");
                        // try to load the group within DSpace
                        Group ldapGroup = null;
                        try {
                            ldapGroup = Group.findByName(context, dspaceGroupName);
                        } catch (SQLException ex) {
                            log.warn("Caught an SQLException, while trying to find ldap special group '" + groupName
                                + "'", ex);
                        }
                        if (ldapGroup != null) {
                            // group does exists, add its ID to the list of special groups
                            specialGroups.add(ldapGroup.getID());
                            log.debug("added group " + dspaceGroupName + " : " + ldapGroup.getID());
                        } else {
                            // The group could not be found. Warn in the logs, but continue without exception.
                            log.warn(LogManager.getHeader(context,
                                "ldap_assignGroups",
                                "Group defined in authentication-ldap.login.groupmap." + i +
                                    " does not exist :: " + dspaceGroupName));
                        }
                    }
                }
    
                // Get the next mapping in the list (will be checked for null by the while loop)
                groupMap = ConfigurationManager.getProperty("authentication-ldap",
                        "login.groupmap." + (++i));
            } // end of while loop
            
            // transform our list of integers into an int array
            int[] groupIds = new int[specialGroups.size()];
            Iterator<Integer> it = specialGroups.iterator();
            for (int j = 0; it.hasNext(); j++) {
                groupIds[j] = it.next();
                log.debug("added special group ID to array: " + groupIds[j]);
            }
            log.debug("found " + specialGroups.size() + " special groups.");
            
            // store the groups we found as request attribute. The method getSpecialGroups will read those
            request.getSession().setAttribute("ldap.specialgroups", groupIds);
        }
    }

    /**
     * Authenticate the given credentials against an LDAP directory.
     * The logic in this method differs from the 5.10 version of this plugin - it is a more simple approach
     * that does not use the SpeakerToLDAP subclass, and it tries the search before a second "bind as user".
     *
     * If 'autoregister' is enabled, a new EPerson is created when a user logs in for the first time.
     * If 'autoupdate' is enabled, the EPerson is updated with LDAP attributes at each login so changes to
     *      email address, name, telephone number are propagated to DSpace from the directory (new feature)
     *
     * @param context
     *  DSpace context, will be modified (ePerson set) upon success.
     *
     * @param netid
     *  LDAP netid field (eg. uid)
     *
     * @param password
     *  User password
     *
     * @param realm
     *  Realm is not used in LDAP authentication and is ignored
     *
     * @param request
     *  The HTTP request that started this operation, or null if not applicable.
     *
     * @return One of:
     *   SUCCESS, BAD_CREDENTIALS, CERT_REQUIRED, NO_SUCH_USER, BAD_ARGS
     * <p>Meaning:
     * <br>SUCCESS         - authenticated OK.
     * <br>BAD_CREDENTIALS - user exists, but credentials (e.g. passwd) don't match
     * <br>CERT_REQUIRED   - not allowed to login this way without X.509 cert.
     * <br>NO_SUCH_USER    - user not found using this method.
     * <br>BAD_ARGS        - user/pw not appropriate for this method
     */
    public int authenticate(Context context,
                            String netid,
                            String password,
                            String realm,
                            HttpServletRequest request) throws SQLException {

        // Are EPerson objects allowed to update their metadata on login? (eg. update email address, names from LDAP)
        boolean autoUpdate = ConfigurationManager.getBooleanProperty(
            "authentication-ldap", "autoupdate");

        log.info(LogManager.getHeader(context, "auth", "attempting trivial auth of user=" + netid));
        
        // Return "bad arguments" result code if either credential is null
        if (netid == null || password == null) {
            return BAD_ARGS;
        }

        // Resolve netid to EPerson. If it doesn't exist, check canSelfRegister() to see if new accounts may
        // be created after authentication. If it is not enabled, then we can return NO_SUCH_USER right here.
        EPerson eperson = null;
        try {
            eperson = EPerson.findByNetid(context, netid.toLowerCase());
            if(eperson == null && !canSelfRegister(context, request, netid)) {
                log.warn("EPerson for netid " + netid + " does not exist, no self-registration enabled");
                return NO_SUCH_USER;
            } else if(eperson != null) {
                log.debug("EPerson for netid " + netid + " found.");
            } else {
                log.debug("Eperson for netid " + netid + " not found, can self-register.");
            }
        } catch (SQLException e) {
            // Handle a SQL exception in findByNetid as a "not found"
            if(!canSelfRegister(context, request, netid)) {
                log.debug("EPerson for netid " + netid + " does not already exist");
                return NO_SUCH_USER;
            }
        }

        // Search LDAP for the user object
        // The searchLDAP() method replaces the getDNOfUser() method used in the v5.10 LDAPAuthentication plugin.
        // As it should be possible to already construct the DN for a user in a flat directory structure, and as
        // the main business of the method is actually retrieving user attributes and returning them, the name
        // is now more appropriate.
        // Inspect this method to see how the code is rewritten to support search by the authenticating user
        LDAPResult ldapResult = searchLDAP(context, netid, password);

        if(ldapResult == null) {
            log.info(LogManager
                .getHeader(context, "failed_login", "no DN found for user " + netid));
            return BAD_CREDENTIALS;
        } else {
            // Result found!
            log.debug("Found LDAP result: " + ldapResult.toString());
            if(eperson != null) {
                // Authenticate as existing user.
                // If the user was already bound in the search, or passes subsequent authentication then
                // treat the login as successful. Since Java uses short-circuit evaluation in logical statements,
                // the "authenticateOnly" request is only performed if the user didn't already bind in the search.
                if(ldapResult.selfBound() || authenticateOnly(context, ldapResult.getDN(), password)) {
                    try {
                        // Temporarily switch off authorisation system while we create/update EPerson objects
                        context.turnOffAuthorisationSystem();

                        if (autoUpdate) {
                            // If update EPerson metadata is allowed, update all LDAP metadata (but not Can Log In)
                            eperson = updateEPerson(context, eperson, ldapResult);
                        }

                        // Always ensure Net ID is correct, either way
                        eperson.setNetid(netid.toLowerCase());
                        context.setCurrentUser(eperson);

                        // Assign user to groups based on configured group or DN
                        assignGroups(ldapResult.getDN(), ldapResult.getLdapGroups(), context, request);

                        // Update EPerson and set current context user (this is the ultimate result of "logging in")
                        eperson.update();
                        return SUCCESS;

                    } catch(AuthorizeException e) {
                        // This error is not quite correct but it's the closet thing to an authorize exception
                        // while trying to update the user
                        log.error(LogManager.getHeader(context,
                            "Could not update eperson metadata", e.getMessage()));
                        return NO_SUCH_USER;

                    } finally {
                        // Commit context and restore authorisation state
                        context.commit();
                        context.restoreAuthSystemState();
                    }
                } else {
                    // Authentication for a searched user failed
                    return BAD_CREDENTIALS;
                }
            } else if (authenticateOnly(context, ldapResult.getDN(), password)) {
                // Authenticate and create new EPerson
                if(canSelfRegister(context, request, netid)) {
                    log.info(LogManager.getHeader(context,
                        "autoregister", "netid=" + netid));
                    try {
                        // Temporarily switch off authorisation system while we create/update EPerson objects
                        context.turnOffAuthorisationSystem();

                        // Create a new EPerson object
                        eperson = EPerson.create(context);

                        // Set basic LDAP metadata
                        eperson = updateEPerson(context, eperson, ldapResult);

                        // Set the Net ID
                        eperson.setNetid(netid.toLowerCase());

                        // Allow the user to login
                        eperson.setCanLogIn(true);

                        // Initialise the EPerson with all other authentication plugins in the stack
                        AuthenticationManager.initEPerson(context, request, eperson);

                        // Update EPerson and set current context user (this is the ultimate result of "logging in")
                        eperson.update();
                        context.setCurrentUser(eperson);

                        // Assign user to groups based on configured group or DN
                        assignGroups(ldapResult.getDN(), ldapResult.getLdapGroups(), context, request);

                    } catch (AuthorizeException e) {
                        return NO_SUCH_USER;
                    } finally {
                        // Commit context and restore authorisation state
                        context.commit();
                        context.restoreAuthSystemState();
                    }

                    log.info(LogManager.getHeader(context, "authenticate",
                        "type=ldap-login, created ePerson"));
                    return SUCCESS;
                }
            } else {
                return BAD_CREDENTIALS;
            }

        }

        // If we reached this far, return a bad argument error as we should have already authenticated
        return BAD_ARGS;
    }

    /**
     * Just authenticate as the user - used after an LDAP Result was returned
     * by an anonymous or "search account" search
     * @param context
     * @param dn
     *  DN is the full DN, returned from the LDAP search, not constructed
     * @param password
     * @return true or false
     */
    protected boolean authenticateOnly(Context context, String dn, String password) {

        String providerUrl = ConfigurationManager.getProperty("authentication-ldap", "provider_url");

        if (password != null && !password.equals("")) {
            // Set up environment for creating initial context
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(javax.naming.Context.PROVIDER_URL, providerUrl);

            // Set authentication credentials
            env.put(javax.naming.Context.SECURITY_AUTHENTICATION, "Simple");
            env.put(javax.naming.Context.SECURITY_PRINCIPAL, dn);
            env.put(javax.naming.Context.SECURITY_CREDENTIALS, password);
            env.put(javax.naming.Context.AUTHORITATIVE, "true");
            env.put(javax.naming.Context.REFERRAL, "follow");

            DirContext ctx = null;
            try {
                // Try to bind to LDAP as the user
                ctx = new InitialDirContext(env);
            } catch (NamingException e) {
                log.warn(LogManager.getHeader(context,
                    "ldap_authentication", "type=failed_auth " + e));
                return false;
            } finally {
                // Close the context when we're done
                try {
                    if (ctx != null) {
                        ctx.close();
                    }
                } catch (NamingException e) {
                    log.error(LogManager.getHeader(context,
                        "ldap_authentication", "type=ldap_error" + e));
                }
            }
        } else {
            return false;
        }

        // We should only reach here if password wasn't null and an exception wasn't thrown binding to the directory
        return true;

    }

    /**
     * This method binds to LDAP - either as a search user, anonymous, or the authenticating user, and searches
     * the directory for the user record to retrieve attributes.
     *
     * This replaces SpeakerToLDAP.getDNOfUser() used in the v5.10 plugin and is called in a different order by
     * the main authentication method, since a second bind to the directory will sometimes be unnecessary.
     *
     * Previously, the LDAP search always ran as either the "search user" or as anonymous. Now, self-bind is
     * also possible, by constructing a DN from netid and object_context instead of searching search_context for
     * the netid. Note this will only work properly with search_scope of 0 or 2.
     *
     * Since a search as the user already achieves LDAP authentication, when this is the case a flag called
     * "bind" on the LDAPResult is set to true.
     *
     * @param context
     * @param netid
     * @param password
     * @return a simple LDAPResult bean containing LDAP attributes and some other indicators
     */
    protected LDAPResult searchLDAP(Context context, String netid, String password) {

        // Search configuration, used for locating the user DN and record
        String searchUser = ConfigurationManager.getProperty(
            "authentication-ldap", "search.user");
        String searchPassword = ConfigurationManager.getProperty(
            "authentication-ldap", "search.password");
        boolean anonymousSearch = ConfigurationManager.getBooleanProperty(
            "authentication-ldap", "search.anonymous");
        String searchContext = ConfigurationManager.getProperty(
            "authentication-ldap", "search_context");
        String objectContext = ConfigurationManager.getProperty(
            "authentication-ldap", "object_context");
        String searchScopeConfiguration = ConfigurationManager.getProperty(
            "authentication-ldap", "search_scope");
        String providerUrl = ConfigurationManager.getProperty(
            "authentication-ldap", "provider_url");

        // Attribute fields
        String ldapIdField = ConfigurationManager.getProperty(
            "authentication-ldap", "id_field");
        String ldapEmailField = ConfigurationManager.getProperty(
            "authentication-ldap", "email_field");
        String ldapGivenNameField = ConfigurationManager.getProperty(
            "authentication-ldap", "givenname_field");
        String ldapSurnameField = ConfigurationManager.getProperty(
            "authentication-ldap", "surname_field");
        String ldapPhoneField = ConfigurationManager.getProperty(
            "authentication-ldap", "phone_field");
        String ldapGroupField = ConfigurationManager.getProperty(
            "authentication-ldap", "login.groupmap.attribute");

        // Set up searchDN and ldapResult objects
        String searchDN = null;
        LDAPResult ldapResult = new LDAPResult();

        // Parse search scope (0, 1 or 2)
        // The search scope to use (default to 0)
        int searchScope = 0;
        try {
            searchScope = Integer.parseInt(searchScopeConfiguration.trim());
        } catch (NumberFormatException e) {
            // Log the error if it has been set but is invalid
            if (searchScopeConfiguration != null) {
                log.warn(LogManager.getHeader(context,
                    "ldap_authentication", "invalid search scope: " + searchScopeConfiguration));
            }
        }

        // If search user credentials are missing and anonymous search is not enabled, we will attempt
        // to construct the searchDN and perform the search as the user. This only works with a flat user structure
        // in the LDAP directory (ie. all users are beneath the OU defined as object_context and search_context)
        // and search_scope 0 or 2, because we will change search_context to match the DN
        boolean selfSearch = ((searchUser == null || searchPassword == null) && !anonymousSearch);

        // Set up environment for creating initial context
        Hashtable env = new Hashtable(11);
        env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(javax.naming.Context.PROVIDER_URL, providerUrl);

        // Find the best way to search the directory for the full DN of the user with supplied netid
        if ((searchUser != null) && (!searchUser.trim().equals(""))
                && (searchPassword != null) && (!searchPassword.trim().equals(""))) {
            // Use "search user" credentials for search
            log.debug("Binding as " + searchUser + " for LDAP user search");
            env.put(javax.naming.Context.SECURITY_AUTHENTICATION, "simple");
            env.put(javax.naming.Context.SECURITY_PRINCIPAL, searchUser);
            env.put(javax.naming.Context.SECURITY_CREDENTIALS, searchPassword);
        } else if(anonymousSearch) {
            // Use anonymous authentication for search
            log.debug("Binding as anonymous for LDAP user search");
            env.put(javax.naming.Context.SECURITY_AUTHENTICATION, "none");
        } else {
            // Construct DN manually, and use user credentials for search
            log.debug("Binding as " + netid + " for LDAP self-search");
            searchDN = ldapIdField + "=" + netid + "," + objectContext;
            // We should also set the searchContext to be this DN itself as this is the best way to guarantee
            // the user has permission to search for itself in LDAP
            searchContext = searchDN;
            log.debug("Constructed DN for " + netid + ": " + searchDN);
            env.put(javax.naming.Context.SECURITY_PRINCIPAL, searchDN);
            env.put(javax.naming.Context.SECURITY_CREDENTIALS, password);
            env.put(javax.naming.Context.AUTHORITATIVE, "true");
            env.put(javax.naming.Context.REFERRAL, "follow");
        }

        // Create a new Directory Context object. This is the service interface to an LDAP directory.
        DirContext ctx = null;

        try {
            // Create initial context - this method uses the credentials and options in the environment hashmap
            // to bind to the directory, and return the resulting context object.
            ctx = new InitialDirContext(env);

            // Set attributes and controls to perform the LDAP search
            // (eg. 'uid={username}' in scope 'ou=users,dc=example,dc=org)
            Attributes matchAttrs = new BasicAttributes(true);
            matchAttrs.put(new BasicAttribute(ldapIdField, netid));
            SearchControls ctrls = new SearchControls();
            // Explicitly set returning attributes so we can always check them even if they're not default names
            String[] returningAttributes = {
                ldapIdField, ldapGivenNameField, ldapSurnameField, ldapEmailField, ldapPhoneField, ldapGroupField
            };
            ctrls.setReturningAttributes(returningAttributes);
            ctrls.setSearchScope(searchScope);
            NamingEnumeration<SearchResult> answer = ctx.search(
                providerUrl + searchContext,
                "(&({0}={1}))", new Object[] { ldapIdField,
                    netid }, ctrls);

            // Iterate results (hopefully there is just one result) and process them into the LDAPResult object
            while (answer.hasMoreElements()) {
                SearchResult sr = answer.next();

                log.debug("SR Object: " + sr.toString());

                // If the search context is empty or if we've already constructed a full DN
                // for search context (self-search) then don't try to append any additional context
                if (StringUtils.isEmpty(searchContext) || selfSearch) {
                    // When self-authenticating, the search context is effectively the full DN and the search result
                    // doesn't contain the "uid=xxx" match name that the admin or anonymous searches contain
                    // so we will set the result DN to be the same as the DN we searched for
                    ldapResult.setDN(searchDN);
                } else {
                    ldapResult.setDN((sr.getName() + "," + searchContext));
                }
                log.debug("LDAP Search Result DN: " + ldapResult.getDN());

                Attributes resultAttributes = sr.getAttributes();

                // Set email if the field is defined and appears in results
                if(ldapEmailField != null && resultAttributes.get(ldapEmailField) != null) {
                    ldapResult.setLdapEmail((String)resultAttributes.get(ldapEmailField).get());
                }
                // Set given name if the field is defined and appears in results
                if(ldapGivenNameField != null && resultAttributes.get(ldapGivenNameField) != null) {
                    ldapResult.setLdapGivenName((String)resultAttributes.get(ldapGivenNameField).get());
                }
                // Set surname if the field is defined and appears in results
                if(ldapSurnameField != null && resultAttributes.get(ldapSurnameField) != null) {
                    ldapResult.setLdapSurname((String)resultAttributes.get(ldapSurnameField).get());
                }
                // Set phone if the field is defined and appears in results
                if(ldapPhoneField != null && resultAttributes.get(ldapPhoneField) != null) {
                    ldapResult.setLdapPhone((String)resultAttributes.get(ldapPhoneField).get());
                }
                // Set group if the field is defined and appears in results
                if(ldapGroupField != null && resultAttributes.get(ldapGroupField) != null) {
                    NamingEnumeration groups = resultAttributes.get(ldapGroupField).getAll();
                    if (groups != null) {
                        while (groups.hasMore()) {
                            ldapResult.addLdapGroup((String) groups.next());
                        }
                    }
                }

                // If the search result has multiple elements, then something has gone wrong with the searc
                // since the netid + search scope we searched for should be unique
                if (answer.hasMoreElements()) {
                    log.error("Multiple results returned for LDAP search: '" + ldapIdField + "'=" + netid +
                        " (" + searchContext + "), results should be unique. Ignoring results.");
                } else {
                    log.debug(LogManager.getHeader(context, "Found LDAP object: ", ldapResult.getDN()));
                    // If this user bound to LDAP with their own credentials, set the flag in LDAPResult
                    // so we can skip further unnecessary authentication
                    ldapResult.setBound(selfSearch);
                    return ldapResult;
                }
            }

            // No results, return null
            return null;

        } catch(NamingException e) {
            log.error(LogManager.getHeader(context, "Error searching LDAP: ", e.getMessage()));
        } finally {
            // Close the LDAP context
            try {
                if (ctx != null) {
                    ctx.close();
                }
            } catch (NamingException e) {
                log.error(LogManager.getHeader(context, "Error closing LDAP context: ", e.getMessage()));
            }
        }

        // If we reached here, we already failed to return a good LDAP result, so return null
        return null;
    }

    /**
     * Is the user allowed to self-register?
     * In the context of LDAP, this means "can a successful login create a new EPerson object"
     */
    @Override
    public boolean canSelfRegister(Context context,
                                   HttpServletRequest request,
                                   String username)
        throws SQLException {

        // Return the "autoregister" boolean from configuration.
        return ConfigurationManager.getBooleanProperty("authentication-ldap", "autoregister");
    }

    /**
     * Add authenticated users to the group defined in dspace.cfg by
     * the login.specialgroup and login.groupmap.* keys. This method respects the login.groupmap parameters as the
     * documentation states that those groups shall be assigned "for the remainder of their current session", which only
     * special groups does.
     */
    @Override
    public int[] getSpecialGroups(Context context, HttpServletRequest request) {
        // check if we have a user that is logged in using LDAP and if they got assigned
        // special group on their login
        if ( request == null ||
                context.getCurrentUser() == null ||
                context.getCurrentUser().getNetid() == null ||
                context.getCurrentUser().getNetid().equals("")) {
            log.debug("Null request or current user or netID, not checking for special groups");
            return new int[0];
        }

        int[] groupIds = (int[]) request.getSession().getAttribute("ldap.specialgroups");
        if(groupIds == null || groupIds.length == 0) {
            log.debug("No special groups found for user");
            return new int[0];
        }

        // cache again, as shib does..?
        request.getSession().setAttribute("ldap.specialgroups", groupIds);

        // return
        log.debug(groupIds.length + " special groups found for user");
        return (groupIds);
    }

    /**
     *  Initialise new EPerson
     */
    @Override
    public void initEPerson(Context context, HttpServletRequest request,
                            EPerson eperson)
        throws SQLException {
        // We have to implement this method because of the AuthenticationMethod interface, but the actual
        // initialisation of the EPerson is all done in the authenticate() method and requires more data than
        // what is available in context, request, eperson.
        // This has no adverse affect - it simply means that when other methods call AuthenticationManager.initEPerson()
        // then this method won't do anything additional to initialise the EPerson.
    }

    /**
     * Specify whether this method is implicit (environmental credentials) or explicit (credentials supplied)
     */
    @Override
    public boolean isImplicit() {
        // LDAP authentication is explicit: we always want a username and password for authentication,
        // so this method should return false.
        return false;
    }

    /**
     * Returns message key for title of the "login" page, to use
     * in a menu showing the choice of multiple login methods.
     */
    @Override
    public String loginPageTitle(Context context) {
        return "org.dspace.eperson.LDAPAuthentication.title";
    }

    /**
     * Returns URL to which to redirect to obtain credentials (either password
     * prompt or e.g. HTTPS port for client cert.); null means no redirect.
     */
    @Override
    public String loginPageURL(Context context,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        return response.encodeRedirectURL(request.getContextPath() +
            "/ldap-login");
    }

    /**
     * Update EPerson metadata based on attributes in an LDAPResult object
     * @return updated EPerson object
     */
    public EPerson updateEPerson(Context context, EPerson eperson, LDAPResult ldapResult) {
        if (StringUtils.isNotEmpty(ldapResult.getLdapEmail())) {
            eperson.setEmail(ldapResult.getLdapEmail());
        }
        if (StringUtils.isNotEmpty(ldapResult.getLdapGivenName())) {
            eperson.setFirstName(ldapResult.getLdapGivenName());
        }
        if (StringUtils.isNotEmpty(ldapResult.getLdapSurname())) {
            eperson.setLastName(ldapResult.getLdapSurname());
        }
        if (StringUtils.isNotEmpty(ldapResult.getLdapPhone())) {
            eperson.setMetadata("phone", ldapResult.getLdapPhone());
        }
        return eperson;

    }
}
