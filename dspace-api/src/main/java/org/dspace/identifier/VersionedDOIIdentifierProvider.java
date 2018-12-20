/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.identifier;

import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Metadatum;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.identifier.doi.DOIConnector;
import org.dspace.identifier.doi.DOIIdentifierException;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.versioning.Version;
import org.dspace.versioning.VersionDAO;
import org.dspace.versioning.VersionHistory;
import org.dspace.versioning.VersionHistoryDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

/**
 * Provide service for DOIs using DataCite.
 * 
 * <p>This class handles reservation, registration and deletion of DOIs using
 * the direct API from {@link <a href="http://www.datacite.org">DataCite</a>}.
 * Please pay attention that some members of DataCite offer special services
 * and want their customers to use special APIs. If you are unsure ask your
 * registration agency.</p>
 * 
 * <p>Any identifier a method of this class returns is a string in the following format: doi:10.123/456.</p>
 * 
 * @author Pascal-Nicolas Becker
 */
public class VersionedDOIIdentifierProvider
    extends DOIIdentifierProvider
{
    private static final Logger log = LoggerFactory.getLogger(VersionedDOIIdentifierProvider.class);

    protected DOIConnector connector;
    protected VersionDAO versionDAO;
    protected VersionHistoryDAO versionHistoryDAO;

    static final char DOT = '.';
    protected static final String pattern = "\\d+\\" + String.valueOf(DOT) +"\\d+";

    public VersionedDOIIdentifierProvider()
    {
        super();
        this.versionDAO = new VersionDAO();
        this.versionHistoryDAO = new VersionHistoryDAO();
    }

    @Autowired
    @Required
    public void setDOIConnector(DOIConnector connector)
    {
        super.setDOIConnector(connector);
        this.connector = connector;
    }

    @Override
    public String register(Context context, DSpaceObject dso)
            throws IdentifierException
    {
        String doi = mint(context, dso);
        // register tries to reserve doi if it's not already.
        // So we don't have to reserve it here.
        this.register(context, dso, doi);
        return doi;
    }


    @Override
    public String mint(Context context, DSpaceObject dso)
            throws IdentifierException
    {
        if (!(dso instanceof Item))
        {
            throw new IdentifierException("Currently only Items are supported for DOIs.");
        }
        Item item = (Item) dso;

        VersionHistory history = versionHistoryDAO.find(context, item.getID(), this.versionDAO);

        String doi = null;
        try
        {
            doi = getDOIByObject(context, dso);
            if (doi != null)
            {
                return doi;
            }
        }
        catch (SQLException e)
        {
            log.error("Error while attemping to retrieve information about a DOI for "
                              + dso.getTypeText() + " with ID " + dso.getID() + ".");
            throw new RuntimeException("Error while attempting to retrieve " +
                                               "information about a DOI for " + dso.getTypeText() +
                                               " with ID " + dso.getID() + ".", e);
        }

        // check whether we have a DOI in the metadata and if we have to remove it
        String metadataDOI = getDOIOutOfObject(dso);
        if (metadataDOI != null)
        {
            // check whether doi and version number matches
            String bareDOI = getBareDOI(metadataDOI);
            int versionNumber = versionDAO.findByItem(context, item).getVersionNumber();

            String versionedDOI = bareDOI;
            if (versionNumber > 1)
            {
                versionedDOI = bareDOI.concat(String.valueOf(DOT)).concat(String.valueOf(versionNumber));
            }
            if (! metadataDOI.equalsIgnoreCase(versionedDOI))
            {
                log.debug("Will remove DOI " + metadataDOI + " from item metadata, as it should become " + versionedDOI + ".");
                // remove old versioned DOIs
                try
                {
                    removePreviousVersionDOIsOutOfObject(context, item, metadataDOI);
                }
                catch (AuthorizeException ex)
                {
                    throw new RuntimeException(
                            "Trying to remove an old DOI from a versioned item, but wasn't authorized to.",
                            ex);
                }
            } else {
                log.debug("DOI " + doi + " matches version number " + versionNumber + ".");
                // ensure DOI exists in our database as well and return.
                // this also checks that the doi is not assigned to another dso already.
                try
                {
                    loadOrCreateDOI(context, dso, versionedDOI);
                }
                catch (SQLException ex)
                {
                    log.error("A problem with the database connection occurd while processing DOI " + versionedDOI + ".",
                              ex);
                    throw new RuntimeException("A problem with the database connection occured.", ex);
                }
                return versionedDOI;
            }
        }

        try {
            if (history != null)
            {
                // versioning is currently supported for items only
                // if we have a history, we have a item
                doi = makeIdentifierBasedOnHistory(context, dso, history);
            } else {
                doi = loadOrCreateDOI(context, dso, null).getStringColumn("doi");
            }
        } catch(SQLException ex) {
            log.error("SQLException while creating a new DOI: ", ex);
            throw new IdentifierException(ex);
        } catch (AuthorizeException ex) {
            log.error("AuthorizationException while creating a new DOI: ", ex);
            throw new IdentifierException(ex);
        }
        return doi;
    }

    @Override
    public void register(Context context, DSpaceObject dso, String identifier)
            throws IdentifierException
    {
        if (!(dso instanceof Item))
        {
            throw new IdentifierException("Currently only Items are supported for DOIs.");
        }
        Item item = (Item) dso;

        if (StringUtils.isBlank(identifier))
        {
            identifier = mint(context, dso);
        }
        String doi = DOI.formatIdentifier(identifier);

        TableRow doiRow = null;

        // search DOI in our db
        try
        {
            doiRow = loadOrCreateDOI(context, dso, doi);
        } catch (SQLException ex) {
            log.error("Error in databse connection: " + ex.getMessage());
            throw new RuntimeException("Error in database conncetion.", ex);
        }

        if (DELETED == doiRow.getIntColumn("status") ||
                TO_BE_DELETED == doiRow.getIntColumn("status"))
        {
            throw new DOIIdentifierException("You tried to register a DOI that "
                    + "is marked as DELETED.", DOIIdentifierException.DOI_IS_DELETED);
        }

        // Check status of DOI
        if (IS_REGISTERED == doiRow.getIntColumn("status"))
        {
            return;
        }

        String metadataDOI = getDOIOutOfObject(dso);
        if (StringUtils.isNotBlank(metadataDOI)
                && !metadataDOI.equalsIgnoreCase(doi))
        {
            // remove older version from metadata
            try {
                removePreviousVersionDOIsOutOfObject(context, item, metadataDOI);
            } catch (AuthorizeException ex) {
                throw new RuntimeException("Trying to remove an old DOI from a versioned item, but wasn't authorized to.", ex);
            }
        }
        
        // change status of DOI
        doiRow.setColumn("status", TO_BE_REGISTERED);
        try {
            DatabaseManager.update(context, doiRow);
        }
        catch (SQLException sqle)
        {
            log.warn("SQLException while changing status of DOI {} to be registered.", doi);
            throw new RuntimeException(sqle);
        }
    }

    /*
     * DSpace creates DOIs by taking the prefix, a slash, a namespace separator and a number. If a version of an item is
     * created, a dot and the version number will be added. As a namesapceseparator may contain dots and numbers, we
     * cannot say if a DOI ends by a number a dot and and number because the namespaceseparator ends on a dot and a
     * number, or because it is a versioned DOI. To get the DOI without the added version number, we have to check all
     * possible namespacesparators. We assume that the longest namespaceseparator is the most exact one. This all works
     * fine, as long as no namespaceseparator can be created out of any other namespaceseparator just by adding a
     * number, a dot and a number, or a number and a dot and a number.
     */
    protected String getBareDOI(String identifier)
            throws DOIIdentifierException
    {
        identifier = DOI.formatIdentifier(identifier);
        String identifierPrefix = DOI.SCHEME.concat(getPrefix())
                                     .concat(String.valueOf(SLASH))
                                     .concat(getNamespaceSeparator(identifier));
        String identifierPostfix = identifier.substring(identifierPrefix.length());
        if (identifierPostfix.matches(pattern) && identifierPostfix.lastIndexOf(DOT) != -1)
        {
            return identifierPrefix.concat(identifierPostfix.substring(0, identifierPostfix.lastIndexOf(DOT)));
        }
        // if the pattern does not match, we are already working on a bare handle.
        return identifier;
    }

    // Should never return null!
    protected String makeIdentifierBasedOnHistory(Context context, DSpaceObject dso, VersionHistory history)
            throws AuthorizeException, SQLException, DOIIdentifierException
    {
        // Mint foreach new version an identifier like: 12345/100.versionNumber
        // use the bare handle (g.e. 12345/100) for the first version.

        // currently versioning is supported for items only
        if (!(dso instanceof Item))
        {
            throw new IllegalArgumentException("Cannot create versioned handle for objects other then item: Currently versioning supports items only.");
        }
        Item item = (Item)dso;
        Version version = null;
        // get the DOI of any previous version
        String previousVersionDOI = null;

        for (Version v : history.getVersions())
        {
            if (v.getItem().getID() == item.getID())
            {
                version = v;
            }
            if (previousVersionDOI == null)
            {
                previousVersionDOI = getDOIByObject(context, v.getItem());
            }
            if (null != previousVersionDOI && version != null)
            {
                break;
            }
        }

        if (previousVersionDOI == null)
        {
            // We need to generate a new DOI.
            TableRow doiRow = DatabaseManager.create(context, "Doi");

            // as we reuse the DOI ID, we do not have to check whether the DOI exists already.
            String identifier = this.getPrefix() + "/" + this.getNamespaceSeparator(context, dso)
                    +  doiRow.getIntColumn("doi_id");

            if (version.getVersionNumber() > 1)
            {
                identifier = identifier.concat(String.valueOf(DOT).concat(String.valueOf(version.getVersionNumber())));
            }

            doiRow.setColumn("doi", identifier);
            doiRow.setColumn("resource_type_id", dso.getType());
            doiRow.setColumn("resource_id", dso.getID());
            doiRow.setColumnNull("status");
            if (0 == DatabaseManager.update(context, doiRow))
            {
                throw new RuntimeException("Cannot save DOI to databse for unkown reason.");
            }

            return identifier;
        }
        assert(previousVersionDOI != null);

        String identifier = getBareDOI(previousVersionDOI);

        if (version.getVersionNumber() > 1)
        {
            identifier = identifier.concat(String.valueOf(DOT))
                                   .concat(String.valueOf(version.getVersionNumber()));
        }

        loadOrCreateDOI(context, dso, identifier);
        return identifier;
    }

    void removePreviousVersionDOIsOutOfObject(Context c, Item item, String oldDoi)
            throws IdentifierException, AuthorizeException
    {
        if (StringUtils.isEmpty(oldDoi))
        {
            throw new IllegalArgumentException("Old DOI must be neither empty nor null!");
        }

        String bareDoi = getBareDOI(DOI.formatIdentifier(oldDoi));
        String bareDoiRef = DOI.DOIToExternalForm(bareDoi);

        Metadatum[] identifiers = item.getMetadata(MD_SCHEMA, DOI_ELEMENT, DOI_QUALIFIER, Item.ANY);
        // We have to remove all DOIs referencing previous versions. To do that,
        // we store all identifiers we do not know in an array list, clear
        // dc.identifier.uri and add the safed identifiers.
        // The list of identifiers to safe won't get larger then the number of
        // existing identifiers.
        ArrayList<String> newIdentifiers = new ArrayList<String>(identifiers.length);
        boolean changed = false;
        for (Metadatum identifier : identifiers)
        {
            if (!StringUtils.startsWithIgnoreCase(identifier.value, bareDoiRef))
            {
                newIdentifiers.add(identifier.value);
            } else {
                changed = true;
            }
        }
        // reset the metadata if neccessary.
        if (changed)
        {
            try
            {
                item.clearMetadata(MD_SCHEMA, DOI_ELEMENT, DOI_QUALIFIER, Item.ANY);
                item.addMetadata(MD_SCHEMA, DOI_ELEMENT, DOI_QUALIFIER, null, newIdentifiers.toArray(new String[] {}));
                item.update();
            } catch (SQLException ex) {
                throw new RuntimeException("A problem with the database connection occured.", ex);
            }
        }
    }
}
