/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.components;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dspace.app.webui.util.VersionUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;
import org.dspace.identifier.DOI;
import org.dspace.identifier.IdentifierException;
import org.dspace.identifier.IdentifierService;
import org.dspace.plugin.ItemHomeProcessor;
import org.dspace.plugin.PluginException;
import org.dspace.utils.DSpace;
import org.dspace.versioning.Version;
import org.dspace.versioning.VersionHistory;

public class VersioningItemHome implements ItemHomeProcessor {

	/** log4j category */
	private static Logger log = Logger.getLogger(VersioningItemHome.class);

	@Override
	public void process(Context context, HttpServletRequest request,
			HttpServletResponse response, Item item) throws PluginException,
			AuthorizeException {
		boolean versioningEnabled = ConfigurationManager.getBooleanProperty(
				"versioning", "enabled");
		boolean submitterCanCreateNewVersion = ConfigurationManager.getBooleanProperty(
				"versioning", "submitterCanCreateNewVersion", false);
		boolean newVersionAvailable = false;
		boolean showVersionWorkflowAvailable = false;
		boolean hasVersionButton = false;
		boolean hasVersionHistory = false;

		IdentifierService identifierService = (new DSpace()).getSingletonService(IdentifierService.class);
		VersionHistory history = null;
		List<Version> historyVersions = new ArrayList<Version>();
		String latestVersionIdentifier = null;

		if (versioningEnabled) {
			try {

				// Only show button if this is the latest version of the item, is archived,
				// not in submission, and a new version is allowed to be created
				if (VersionUtil.isLatest(context, item)
					&& item.isArchived()
					&& item.canCreateNewVersion(context)
					&& !VersionUtil.isItemInSubmission(context, item)) {

					if(item.canEdit() || submitterCanCreateNewVersion) {
						hasVersionButton = true;
					}
				}

			} catch (SQLException e) {
				throw new PluginException(e.getMessage());
			}

			if (VersionUtil.hasVersionHistory(context, item)) {
				hasVersionHistory = true;
				history = VersionUtil.retrieveVersionHistory(context, item);
				for(Version versRow : history.getVersions()) {
		            //Skip items currently in submission
		            try {
						if(VersionUtil.isItemInSubmission(context, versRow.getItem())) {
						    continue;
						}
						else {
							historyVersions.add(versRow);
						}
					} catch (SQLException e) {
						throw new PluginException(e.getMessage());
					}
				}
			}

			// Check if we have a history for the item
			Version latestVersion;
			try {
				latestVersion = VersionUtil.checkLatestVersion(context, item);
			} catch (SQLException e) {
				throw new PluginException(e.getMessage());
			}

			if (latestVersion != null
					&& latestVersion != null
					&& latestVersion.getItemID() != item.getID())
			{
				// We have a newer version
				Item latestVersionItem = latestVersion.getItem();
				if (latestVersionItem.isArchived())
				{
					// Available, add a link for the user alerting him that
					// a new version is available
					newVersionAvailable = true;

					// look up the the latest version handle
					String latestVersionHandle = latestVersionItem.getHandle();
					if (latestVersionHandle != null)
					{
						latestVersionIdentifier = HandleManager.getCanonicalForm(latestVersionHandle);
					}

					// lookup the latest version doi
					String latestVersionDOI = null;
					if (identifierService != null)
					{
						latestVersionDOI = identifierService.lookup(context, latestVersionItem, DOI.class);
					}
					if (latestVersionDOI != null)
					{
						try
						{
							latestVersionDOI = DOI.DOIToExternalForm(latestVersionDOI);
						}
						catch (IdentifierException ex)
						{
							log.error("Unable to convert DOI '" + latestVersionDOI + "' into external form: " + ex.toString(),
									  ex);
							throw new PluginException(ex);
						}
					}

					// do we prefer to use handle or DOIs?
					if ("doi".equalsIgnoreCase(ConfigurationManager.getProperty("webui.preferred.identifier")))
					{
						if (latestVersionDOI != null)
						{
							latestVersionIdentifier = latestVersionDOI;
						}
					}
					else
					{
						// We might be dealing with a workflow/workspace item
						showVersionWorkflowAvailable = true;
					}
				}
			}
		}

		request.setAttribute("versioning.enabled", versioningEnabled);
		request.setAttribute("versioning.hasversionbutton", hasVersionButton);
		request.setAttribute("versioning.hasversionhistory", hasVersionHistory);
		request.setAttribute("versioning.history", history);
		request.setAttribute("versioning.historyversions", historyVersions);
		request.setAttribute("versioning.newversionavailable",
				newVersionAvailable);
		request.setAttribute("versioning.showversionwfavailable",
				showVersionWorkflowAvailable);
		request.setAttribute("versioning.latest_version_identifier",
				latestVersionIdentifier);

	}

}
