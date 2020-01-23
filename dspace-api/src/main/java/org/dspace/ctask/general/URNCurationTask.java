/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ctask.general;

import org.apache.log4j.Logger;
import org.dspace.content.Collection;
import org.dspace.content.Metadatum;
import org.dspace.content.MetadataValue;
import org.dspace.content.Bitstream;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;
import org.dspace.workflow.WorkflowItem;
import org.dspace.services.ConfigurationService;
import org.dspace.utils.DSpace;

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

public class URNCurationTask extends AbstractCurationTask
{

    // The status of the link checking of this item
    private int status = Curator.CURATE_UNSET;

    // The results of link checking this item
    private List<String> results = null;

    private static final String NEW_ITEM_HANDLE = "in workflow";

    // The log4j logger for this class
    private static Logger log = Logger.getLogger(TypeSetter.class);


    protected static String getCollectionException()
    {
        String ignorecollection = (new DSpace()).getSingletonService(ConfigurationService.class).getProperty("identifier.urn.ignorecollection");
        return ignorecollection;
    }

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

                Metadatum[] urnFields = item.getMetadata("tuhh", "identifier", "urn", Item.ANY);
                Metadatum[] dcurnFields = item.getMetadata("dc", "identifier", "urn", Item.ANY);

                Collection col = item.getOwningCollection();
                if (col == null)
                {
                    // check if we have a workspace item, they store the collection separately.
                    WorkspaceItem wsi = WorkspaceItem.findByItem(context, item);
                    if (wsi != null)
                    {
                        col = wsi.getCollection();
                    }
                    if (col == null)
                    {
                        // same for the workflow item
                        WorkflowItem wfi = WorkflowItem.findByItem(context, item);
                        if (wfi != null)
                        {
                            col = wfi.getCollection();
                        }
                    }
                }
                String ch = col.getHandle();
                String ignorecoll = getCollectionException();

                log.debug("Collection Handle is "+ch+". Will ignore "+ignorecoll);

                if (ch.equals(ignorecoll) && urnFields.length > 0) {
                    results.append("Deleting URN from ").append(getItemHandle(item));
                    item.clearMetadata("tuhh", "identifier", "urn", Item.ANY);
                    item.clearMetadata("dc", "identifier", "urn", Item.ANY);
                    if (dcurnFields.length > 0) {
                        for (Metadatum dcurnField : dcurnFields) {
                            if (!dcurnField.value.contains("urn:nbn:de:gbv:830")) {
                                item.addMetadata("dc", "identifier", "urn", dcurnField.language, dcurnField.value, null, dcurnField.confidence);
                            }
                    }
                    item.updateMetadata();
                    item.update();
                    context.getDBConnection().commit();
                }
                else {
                    results.append("Nothing to do for ").append(getItemHandle(item));
                }
                status = Curator.CURATE_SUCCESS;

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
