/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.webui.cris.processor;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.dspace.app.cris.integration.DatasetSchemaOrgMetadata;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.plugin.ItemHomeProcessor;
import org.dspace.plugin.PluginException;

/**
 * Processor for exposition of schema.org metadata for Dataset
 *
 */
public class SchemaOrgItemHomeProcessor implements ItemHomeProcessor
{

    @Override
    public void process(Context context, HttpServletRequest request,
            HttpServletResponse response, Item item)
            throws PluginException, AuthorizeException
    {
        boolean schemaOrgEnabled = ConfigurationManager
                .getBooleanProperty("schemaorg-dataset-metadata.enable", false);
        if (schemaOrgEnabled)
        {
            String dcType = item.getMetadata("dc.type");
            if ("Dataset".equalsIgnoreCase(dcType))
            {
                try
                {
                    DatasetSchemaOrgMetadata schemaOrgMetadataWrapper = new DatasetSchemaOrgMetadata(
                            context, item);
                    String json = schemaOrgMetadataWrapper.getJsonAsString();
                    request.setAttribute("schemaOrgJsonLD",
                            json);
                }
                catch (SQLException e)
                {
                    throw new PluginException(e);
                }
            }
        }
    }

}
