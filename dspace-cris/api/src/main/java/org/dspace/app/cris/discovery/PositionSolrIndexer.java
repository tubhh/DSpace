/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.discovery;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.model.jdyna.ACrisNestedObject;
import org.dspace.content.DSpaceObject;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.discovery.SolrServiceIndexPlugin;
import org.dspace.discovery.configuration.DiscoverySearchFilter;

import it.cilea.osd.jdyna.model.ANestedPropertiesDefinition;
import it.cilea.osd.jdyna.model.ANestedProperty;
import it.cilea.osd.jdyna.model.ATypeNestedObject;
import it.cilea.osd.jdyna.model.PropertiesDefinition;
import it.cilea.osd.jdyna.model.Property;

public class PositionSolrIndexer implements SolrServiceIndexPlugin, CrisServiceIndexPlugin
{
    private Logger log = Logger.getLogger(PositionSolrIndexer.class);

    @Override
    public void additionalIndex(Context context, DSpaceObject dso,
            SolrInputDocument document, Map<String, List<DiscoverySearchFilter>> searchFilters)
    {
        int objectPosition = 0;

        String objectsOrder = ConfigurationManager.getProperty("search.objects.order.list");
        if (StringUtils.isNotBlank(objectsOrder))
        {
            List<String> objectsOrderList = Arrays.asList(objectsOrder.split(","));
            int retrieveObjectPosition = objectsOrderList.indexOf(String.valueOf(dso.getType()));
            objectPosition = retrieveObjectPosition == -1 ? objectsOrderList.size() : retrieveObjectPosition;
        }

        document.addField("objectposition", objectPosition);
        document.addField("objectposition_sort", objectPosition);
    }

    @Override
    public <P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>> void additionalIndex(
            ACrisObject<P, TP, NP, NTP, ACNO, ATNO> crisObject, SolrInputDocument solrDoc,
            Map<String, List<DiscoverySearchFilter>> searchFilters)
    {
        additionalIndex(null, crisObject, solrDoc, searchFilters);
    }

    @Override
    public <P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>> void additionalIndex(
            ACNO dso, SolrInputDocument solrDoc, Map<String, List<DiscoverySearchFilter>> searchFilters)
    {
        // NOT SUPPORTED OPERATION
    }
}
