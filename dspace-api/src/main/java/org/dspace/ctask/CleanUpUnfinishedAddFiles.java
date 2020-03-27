package org.dspace.ctask;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bundle;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;
import org.dspace.curate.Distributive;
import org.dspace.eperson.EPerson;
import org.dspace.workflow.WorkflowItem;
import org.dspace.workflow.WorkflowManager;

@Distributive
public class CleanUpUnfinishedAddFiles extends AbstractCurationTask {

    private static String DEFAULT_PENDING_BUNDLE = "PENDING";
    private static String pendingBundle;
    protected List<String> errors = new ArrayList<>();
    protected List<String> results = new ArrayList<>();

    @Override
    public void init(Curator curator, String taskId) throws IOException {
        super.init(curator, taskId);
        pendingBundle = ConfigurationManager.getProperty("submit.fulltext.bundle.pending");
        if (pendingBundle == null) {
            pendingBundle = DEFAULT_PENDING_BUNDLE;
        }
    }

    /**
     * Perform the curation task upon passed DSO.
     *
     * @param dso the DSpace object
     * @throws IOException if IO error
     */
    @Override
    public int perform(DSpaceObject dso) throws IOException
    {
        distribute(dso);
        if (errors.size() > 0)
        {
            StringBuilder errorBuilder = new StringBuilder();
            for (String error : errors)
            {
                report(error);
                errorBuilder.append(error);
                errorBuilder.append("\n");
            }
            setResult(errorBuilder.toString());
            return Curator.CURATE_ERROR;
        }
        return Curator.CURATE_SUCCESS;
    }

    @Override
    protected void performItem(Item item) throws SQLException, IOException {
        // Does this item have pending files AND NOT currently in workflow for review?
        // If so, it is a half-finished submission and we should remove it and reset the submitter
        if (WorkflowManager.isPendingFulltext(item) &&
            WorkflowItem.findByItem(Curator.curationContext(), item) == null) {
            try {
                item.getBundles(pendingBundle);
                for (Bundle b : item.getBundles(pendingBundle)) {
                    item.removeBundle(b);
                }
                item.update();

                // Now, revert submitter to previous
                List<String> v = item.getMetadataValue("tuhh.submitter.previous");
                if (v != null && v.size() > 0) {
                    EPerson previous = EPerson.find(Curator.curationContext(), Integer.parseInt(v.get(0)));
                    item.setSubmitter(previous);
                    WorkflowManager.clearSubmitterMetadata(item);
                    item.update();
                }
                report("Cleaned up unfinished Add Files for " + item.getHandle() + " and reset item submitter");
                results.add("Cleaned up unfinished Add Files for " + item.getHandle() + " and reset item submitter");

            } catch(AuthorizeException e) {
                errors.add("Authorization failed working on item " + item.getHandle() + ": " + e.getLocalizedMessage());
            }
        }

    }

}
