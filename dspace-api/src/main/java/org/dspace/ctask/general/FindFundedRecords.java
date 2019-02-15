/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ctask.general;

import org.apache.log4j.Logger;
import org.dspace.content.Metadatum;
import org.dspace.content.MetadataValue;
import org.dspace.content.Bitstream;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.dspace.authorize.AuthorizeException;
import java.sql.SQLException;


/**
 * Find funded records having a value set for dc.description.sponsorship
 *
 * @author Oliver Goldschmidt
 */

public class FindFundedRecords extends AbstractCurationTask
{

    // The status of the link checking of this item
    private int status = Curator.CURATE_UNSET;

    // The results of link checking this item
    private List<String> results = null;

    // The results that we'll return
    private StringBuilder resultsString = new StringBuilder();

    private static final String NEW_ITEM_HANDLE = "in workflow";

    // The log4j logger for this class
    private static Logger log = Logger.getLogger(TypeSetter.class);


    /**
     * Perform the link checking.
     *
     * @param dso The DSpaaceObject to be checked
     * @return The curation task status of the checking
     * @throws java.io.IOException THrown if something went wrong
     */
    @Override
    public int perform(DSpaceObject dso) throws IOException
    {
        //MetadataValue.PARENT_PLACEHOLDER_VALUE


        // Unless this is  an item, we'll skip this item
        status = Curator.CURATE_SKIP;
        if (dso.getType() == Constants.ITEM)
        {
            Item item = (Item)dso;
            if (!getItemHandle(item).equals(NEW_ITEM_HANDLE)) {
            try {
                Context context = Curator.curationContext();
                Metadatum[] institutes = item.getMetadata("dc", "desciption", "sponsorship", Item.ANY);
                if (institutes.length > 0) {
                    for (Metadatum institute : institutes) {
                        String auth = institute.authority;
                        int conf = (int) institute.confidence;
                        if (auth == null) {
                            resultsString.append("No authority for item ").append(getItemHandle(item)).append(" for funding ou ").append(institute.value).append("!<br/>\n");
                        }
                        else if (conf < 600) {
                            resultsString.append("Unapproved authority ").append(auth).append(" with confidence level ").append(institute.confidence).append(" for item ").append(getItemHandle(item)).append(" for institute ").append(institute.value).append("!<br/>\n");
                        }
                        else {
                            resultsString.append("Approved authority ").append(auth).append(" for funding ou ").append(institute.value).append(" for item ").append(getItemHandle(item)).append(". Looks all good already - no need to change anything :-).<br/>\n");
                        }
                    }
                }

                status = Curator.CURATE_SUCCESS;
            } catch (SQLException sqle) {
                // Something went wrong
                logDebugMessage(sqle.getMessage());
                status = Curator.CURATE_ERROR;
            }
            }
        }

        setResult(resultsString.toString());
        report(resultsString.toString());

        return status;
    }

    /**
     * Debugging logging if required
     *
     * @param message The message to log
     */
    private void logDebugMessage(String message)
    {
        if (log.isDebugEnabled())
        {
            log.debug(message);
        }
    }

    private static String getItemHandle(Item item)
    {
        String handle = item.getHandle();
        return (handle != null) ? handle: NEW_ITEM_HANDLE;
    }

}
