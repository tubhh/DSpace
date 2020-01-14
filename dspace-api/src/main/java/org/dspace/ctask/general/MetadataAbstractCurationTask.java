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
 * This Curation Task cures Abstracts for items and transfers them into the right local field or the dc abstract field.
 *
 * @author Oliver Goldschmidt
 */

public class MetadataAbstractCurationTask extends AbstractCurationTask
{

    // The status of the link checking of this item
    private int status = Curator.CURATE_UNSET;

    // The results of link checking this item
    private List<String> results = null;

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

        // The results that we'll return
        StringBuilder results = new StringBuilder();

        String language = null;

        // Unless this is  an item, we'll skip this item
        status = Curator.CURATE_SKIP;
        if (dso.getType() == Constants.ITEM)
        {
            Item item = (Item)dso;
            if (!getItemHandle(item).equals(NEW_ITEM_HANDLE)) {
            try {
                Context context = Curator.curationContext();

            Metadatum[] abstractGerman = item.getMetadata("tuhh", "abstract", "german", Item.ANY);
            Metadatum[] abstractEnglish = item.getMetadata("tuhh", "abstract", "english", Item.ANY);
            Metadatum[] abstractDc = item.getMetadata("dc", "description", "abstract", Item.ANY);
            // Case 1: abstract in dc is not set
            // get the abstracts from local fields and transfer them into dc field
            if (abstractDc.length == 0 && (abstractGerman.length > 0 || abstractEnglish.length > 0)) {
                if (abstractGerman.length > 0) {
                    item.addMetadata("dc", "description", "abstract", "de", abstractGerman[0].value, null, -1);
                    results.append("Set german abstract from local abstract field into dc.abstract").append("\n");
                }
                if (abstractEnglish.length > 0) {
                    item.addMetadata("dc", "description", "abstract", "en", abstractEnglish[0].value, null, -1);
                    results.append("Set english abstract from local abstract field into dc.abstract").append("\n");
                }
            }
            // Case 2: abstract in dc set, but not in local fields
            // get the abstract from dc field and transfer it into corresponding local field (english|german)
            // if no language is set in dc field, set it to english and store it in local english field
            else if (abstractDc.length > 0) {
                for (Metadatum abs : abstractDc) {
                    try {
                        language = abs.language;
                        if (language.equals("de")) {
                            if (abstractGerman.length > 0) {
                                item.clearMetadata("tuhh", "abstract", "german", Item.ANY);
                            }
                            item.addMetadata("tuhh", "abstract", "german", "de", abs.value, null, -1);
                            results.append("Set german abstract from dc.abstract into local local abstract field").append("\n");
                        } else {
                            if (abstractEnglish.length > 0) {
                                item.clearMetadata("tuhh", "abstract", "english", Item.ANY);
                            }
                            item.addMetadata("tuhh", "abstract", "english", "en", abs.value, null, -1);
                            results.append("Set unqualified abstract from dc.abstract into local local abstract field").append("\n");
                            item.clearMetadata("dc", "description", "abstract", Item.ANY);
                            item.addMetadata("dc", "description", "abstract", "en", abs.value, null, -1);
                            results.append("Set language english to abstract in dc.abstract").append("\n");
                        }
                    }
                    catch (NullPointerException npe) {
                        if (abstractEnglish.length > 0) {
                            item.clearMetadata("tuhh", "abstract", "english", Item.ANY);
                        }
                        item.addMetadata("tuhh", "abstract", "english", "en", abs.value, null, -1);
                        results.append("Set unqualified abstract from dc.abstract into local local abstract field").append("\n");
                        item.clearMetadata("dc", "description", "abstract", Item.ANY);
                        item.addMetadata("dc", "description", "abstract", "en", abs.value, null, -1);
                        results.append("Set language english to abstract in dc.abstract").append("\n");
                    }
                }
            }
            // Case 3: abstract set in local fields and in dc field
            // do nothing
            else {
                results.append("Nothing to do for ").append(getItemHandle(item)).append("\n");
            }
                status = Curator.CURATE_SUCCESS;

                item.updateMetadata();
                item.update();
                context.getDBConnection().commit();
            } catch (AuthorizeException ae) {
                // Something went wrong
                logDebugMessage(ae.getMessage());
                status = Curator.CURATE_ERROR;
            } catch (SQLException sqle) {
                // Something went wrong
                logDebugMessage(sqle.getMessage());
                status = Curator.CURATE_ERROR;
            }
            }
        }

        setResult(results.toString());
        report(results.toString());

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
