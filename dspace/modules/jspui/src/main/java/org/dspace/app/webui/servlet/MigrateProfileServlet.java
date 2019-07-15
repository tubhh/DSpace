/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AUTH;
import org.apache.log4j.Logger;
import org.dspace.app.webui.util.Authenticate;
import org.dspace.app.webui.util.JSPManager;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;

/**
 * Servlet for handling editing user profiles
 * based on EditProfileServlet
 *
 * @author Kim Shepherd
 * @version $Revision$
 */
public class MigrateProfileServlet extends DSpaceServlet
{
    /** Logger */
    private static Logger log = Logger.getLogger(MigrateProfileServlet.class);

    protected void doDSGet(Context context, HttpServletRequest request,
                           HttpServletResponse response) throws ServletException, IOException,
        SQLException, AuthorizeException
    {
        // A GET displays the edit profile form. We assume the authentication
        // filter means we have a user.
        log.info(LogManager.getHeader(context, "view_migrate_profile", ""));

        // Get the user - authentication should have happened
        EPerson eperson = context.getCurrentUser();

        // Find out if this user is shibboleth authenticated
        Boolean shibbolethAuthenticated = (Boolean) request.getSession().getAttribute("shib.authenticated");
        if(shibbolethAuthenticated == null) {
            shibbolethAuthenticated = false;
        }
        boolean shibbolethUsersCanMigrateAccount = ConfigurationManager.getBooleanProperty(
            "authentication-shibboleth", "password.allow-migrate-to-local", false);

        // User must be logged in with shibboleth *and* allowed to migrate to local, or we won't process
        // this request
        if(!shibbolethAuthenticated || !shibbolethUsersCanMigrateAccount) {
            log.info(LogManager.getHeader(context, "view_profile",
                "not shibboleth or not allowed"));

            request.setAttribute("eperson", eperson);
            request.setAttribute("not_permitted", true );

            JSPManager.showJSP(request, response, "/register/migrate-profile.jsp");
            return;
        }

        request.setAttribute("eperson", context.getCurrentUser());

        JSPManager.showJSP(request, response, "/register/migrate-profile.jsp");
    }

    protected void doDSPost(Context context, HttpServletRequest request,
                            HttpServletResponse response) throws ServletException, IOException,
        SQLException, AuthorizeException
    {
        // Get the user - authentication should have happened
        EPerson eperson = context.getCurrentUser();

        // Find out if this user is shibboleth authenticated
        Boolean shibbolethAuthenticated = (Boolean) request.getSession().getAttribute("shib.authenticated");
        if(shibbolethAuthenticated == null) {
            shibbolethAuthenticated = false;
        }
        boolean shibbolethUsersCanChangePassword = ConfigurationManager.getBooleanProperty(
                "authentication-shibboleth","password.allow_change", false);
        boolean shibbolethUsersCanMigrateAccount = ConfigurationManager.getBooleanProperty(
            "authentication-shibboleth", "password.allow-migrate-to-local", false);

        // User must be logged in with shibboleth *and* allowed to migrate to local, or we won't process
        // this request
        if(!shibbolethAuthenticated || !shibbolethUsersCanMigrateAccount) {
            log.info(LogManager.getHeader(context, "view_profile",
                "not shibboleth or not allowed"));

            request.setAttribute("eperson", eperson);
            request.setAttribute("not_permitted", true );

            JSPManager.showJSP(request, response, "/register/migrate-profile.jsp");
            return;
        }

        // As we are migrating, we will always allow setting of password
        boolean settingPassword = true;

        try {
            boolean ok = migrateUserProfile(eperson, request, response, context);

            if (ok) {
                if(settingPassword) {
                    // They want to set a new password.
                    boolean setpw = confirmAndSetPassword(eperson, request);
                    if (!setpw) {
                        request.setAttribute("password.problem", Boolean.TRUE);
                    }
                }

                // Update the DB
                log.info(LogManager.getHeader(context, "migrate_profile",
                    "password_changed=" + settingPassword));
                eperson.update();

                // Since this worked, we should also log the user out
                Authenticate.loggedOut(context, request);

                // Manually invalidate session
                request.getSession().invalidate();

                // Show confirmation
                request.setAttribute("password.updated", settingPassword);
                JSPManager.showJSP(request, response,
                    "/register/profile-migrated.jsp");

                context.complete();
            } // User-level error response handling is now handled in migrateUserProfile

        } catch(AuthorizeException e) {
            log.error(e.getMessage());
            request.setAttribute("eperson", eperson);
            request.setAttribute("not_permitted", true );
            JSPManager.showJSP(request, response, "/register/migrate-profile.jsp");
        }

    }

    /**
     * Migrate a user's profile information with the information in the given
     * request, forcing the account to be treated as local (no netID, set email).
     * This assumes that authentication has occurred. This method
     * doesn't write the changes to the database (i.e. doesn't call update.)
     *
     * @param eperson
     *            the e-person
     * @param request
     *            the request to get values from
     *
     * @return true if the user supplied all the required information, false if
     *         they left something out.
     */
    public static boolean migrateUserProfile(EPerson eperson,
                                             HttpServletRequest request,
                                             HttpServletResponse response,
                                             Context context)
        throws AuthorizeException, SQLException, IOException, ServletException {
        request.setAttribute("eperson", eperson);

        // Get the parameters from the form
        String lastName = request.getParameter("last_name");
        String firstName = request.getParameter("first_name");
        String phone = request.getParameter("phone");
        String language = request.getParameter("language");
        String email = request.getParameter("email");

        // Check database for other eperson objects with this email address
        // TODO - test as non-admin, will this throw authorizeexception?
        EPerson existing = EPerson.findByEmail(context, email);
        if(null != existing && !email.equals(eperson.getEmail())) {
            // Return error - this email address is already associated with an EPerson
            // Could make use of the 'already-registered' JSP?
            log.warn("EPerson already exists in database, cannot migrate " + email);
            request.setAttribute("eperson_exists",true);
            JSPManager.showJSP(request, response, "/register/migrate-profile.jsp");
            return false;
        }

        // Update the eperson
        eperson.setFirstName(firstName);
        eperson.setLastName(lastName);
        eperson.setMetadata("phone", phone);
        eperson.setLanguage(language);
        eperson.setEmail(email);

        // Set NetID to NULL
        eperson.setNetid(null);

        // Check all required fields are there
        if(!StringUtils.isEmpty(lastName)
            && !StringUtils.isEmpty(firstName)
            && !StringUtils.isEmpty(email)) {
            return true;
        }
        else {
            log.warn(LogManager.getHeader(context, "view_migrate_profile",
                "problem=true"));
            request.setAttribute("missing.fields", Boolean.TRUE);
            JSPManager.showJSP(request, response, "/register/migrate-profile.jsp");
            return false;
        }
    }

    /**
     * Set an eperson's password, if the passwords they typed match and are
     * acceptible. If all goes well and the password is set, null is returned.
     * Otherwise the problem is returned as a String.
     *
     * @param eperson
     *            the eperson to set the new password for
     * @param request
     *            the request containing the new password
     *
     * @return true if everything went OK, or false
     */
    public static boolean confirmAndSetPassword(EPerson eperson,
                                                HttpServletRequest request)
    {
        // Get the passwords
        String password = request.getParameter("password");
        String passwordConfirm = request.getParameter("password_confirm");

        // Check it's there and long enough
        if ((password == null) || (password.length() < 6))
        {
            return false;
        }

        // Check the two passwords entered match
        if (!password.equals(passwordConfirm))
        {
            return false;
        }

        // Everything OK so far, change the password
        eperson.setPassword(password);

        return true;
    }
}
