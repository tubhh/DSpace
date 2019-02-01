/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.integration;

import org.apache.log4j.Logger;
import org.dspace.content.Metadatum;
import org.dspace.content.MetadataValue;
import org.dspace.content.Bitstream;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.dspace.core.ConfigurationManager;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.apache.commons.lang.StringUtils;
import org.dspace.utils.DSpace;
import org.dspace.workflow.WorkflowItem;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.commons.validator.routines.UrlValidator;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.dspace.authorize.AuthorizeException;
import java.sql.SQLException;


/**
 * A Consumer to handle relations.
 * It checks the content of a datacite.relation element, which should hold a DOI.
 * If the DOI is a local one, the element is transferred to a corresponding local
 * field, which is authority controlled for local items.
 *
 * @author Oliver Goldschmidt
 */

public class RelationConsumer implements Consumer
{

    // The log4j logger for this class
    private static Logger log = Logger.getLogger(RelationConsumer.class);

    private HttpSolrServer solr = null;

    @Override
    public void initialize() throws Exception {
        // nothing to do
    }

    // as we use asynchronous metadata update, our updates are not very expensive.
    // so we can do everything in the consume method.
    @Override
    public void consume(Context ctx, Event event) throws Exception {
        if (event.getSubjectType() != Constants.ITEM)
        {
            log.warn("RelationConsumer should not have been given this kind of "
                    + "subject in an event, skipping: " + event.toString());
            return;
        }

        String doiprefix = ConfigurationManager.getProperty("identifier.doi.prefix");
        String urnprefix = ConfigurationManager.getProperty("identifier.urn.prefix");
        String hdlprefix = ConfigurationManager.getProperty("handle.prefix");

        DSpaceObject dso = event.getSubject(ctx);
        //MetadataValue.PARENT_PLACEHOLDER_VALUE

        // Unless this is  an item, we'll skip this item
        if (dso.getType() == Constants.ITEM)
        {
            Item item = (Item)dso;
            Map<String, String> reciprocalRelations = getReciprocalRelationsList();
            Metadatum[] relations = item.getMetadata("datacite", "relation", Item.ANY, Item.ANY);
            // Walk through datacite relations and check contents
            if (relations.length > 0) {
                try {
                for (Metadatum relation : relations) {
                    // Cut unwanted identifier type prefix
                    if (relation.value.substring(0,3).equals("doi") || relation.value.substring(0,3).equals("hdl")) {
                        String identifier = relation.value.substring(4).trim();
                        log.info("Checking local items for ID "+identifier);
                        Item relatedItem = null;
                        //Item relatedSolrItem = null;
                        // Search for identifier
                        SolrQuery solrQuery = new SolrQuery()
                            .setQuery("dc.identifier.doi:"+identifier+" OR handle:"+identifier);
                        solrQuery.setFields("search.resourceid");
                        QueryResponse resp = getSolr().query(solrQuery);
                        log.info("Looking for identifier "+identifier+" in Solr... Got "+Long.toString(resp.getResults().getNumFound())+" results.");
                        if (resp.getResults().getNumFound() > 0) {
                            SolrDocumentList resultList = resp.getResults();
                            for (SolrDocument result : resultList) {
                                int internalId = (int)result.getFieldValue("search.resourceid");
                                relatedItem = Item.find(ctx,internalId);
                                log.info("Found related item for "+getItemHandle(item)+" while looking for identifier "+identifier+": "+getItemHandle(relatedSolrItem));
                            }
                        }
                        // TODO: What we are doing here is absolutely not performant. Improve performance!
                        /*
                        ItemIterator itemList = Item.findAll(ctx);
                        while (itemList.hasNext()) {
                            String doi = null;
                            String urn = null;
                            Item it = itemList.next();
                            Metadatum[] doiMd = it.getMetadata("dc", "identifier", "doi", Item.ANY);
                            if (doiMd.length > 0) {
                                doi = doiMd[0].value;
                            }
                            Metadatum[] urnMd = it.getMetadata("dc", "identifier", "urn", Item.ANY);
                            if (urnMd.length > 0) {
                                urn = urnMd[0].value;
                            }
                        String hdl = getItemHandle(it);
                        if ((doi != null && doi.equals(identifier)) || (urn != null && urn.equals(identifier)) || hdl.equals(identifier)) {
                            relatedItem = it;
                            break;
                        }
                        */
                    }
//                    }
                    if (relatedItem != null) {
                        log.info("Found related item for "+getItemHandle(item)+": "+getItemHandle(relatedItem));
                    // Check, if local metadata field is available (necessary?)
                    Metadatum[] titleMd = relatedItem.getMetadata("dc", "title", Item.ANY, Item.ANY);
                    String title = getItemHandle(relatedItem);
                    if (titleMd.length > 0) {
                        title = titleMd[0].value;
                    }
                    Metadatum[] languageMd = relatedItem.getMetadata("dc", "language", "iso", Item.ANY);
                    String lang = null;
                    if (languageMd.length > 0) {
                        lang = languageMd[0].value;
                    }
                    Metadatum[] identifierMd = item.getMetadata("dc", "identifier", "doi", Item.ANY);
                    String id = "hdl:"+getItemHandle(item);
                    if (identifierMd.length > 0) {
                        id = "doi:"+identifierMd[0].value;
                    }
                    // Clear local metadata field
                    item.clearMetadata("local", "relation", reciprocalRelations.get(relation.qualifier), Item.ANY);
                    // transfer content to a corresponding field in local schema
                    item.addMetadata("local", "relation", reciprocalRelations.get(relation.qualifier), lang, title, getItemHandle(relatedItem), 600);
                    // build corresponding datacite field in related item
                    relatedItem.clearMetadata("datacite", "relation", reciprocalRelations.get(relation.qualifier), Item.ANY);
                    relatedItem.addMetadata("datacite", "relation", reciprocalRelations.get(relation.qualifier), null, id, null, -1);
                    relatedItem.updateMetadata();
                    relatedItem.update();
                    }
                    }
                }
                item.updateMetadata();
                item.update();
                ctx.getDBConnection().commit();
                } catch (AuthorizeException ae) {
                        // Something went wrong
                        logDebugMessage(ae.getMessage());
                    } catch (SQLException sqle) {
                        // Something went wrong
                        logDebugMessage(sqle.getMessage());
                    }
            }
        }
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

    /**
     * Internal utitity method to get a description of the handle
     *
     * @param item The item to get a description of
     * @return The handle, or in workflow
     */
    private static String getItemHandle(Item item)
    {
        String handle = item.getHandle();
        return (handle != null) ? handle: " in workflow";
    }

    private static Map<String, String> getReciprocalRelationsList() {
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("Cites","IsCitedBy");
        ret.put("Compiles","IsCompiledBy");
        ret.put("Documents","IsDocumentedBy");
        ret.put("HasPart","IsPartOf");
        ret.put("IsIdenticalTo","IsIdenticalTo");
        ret.put("IsNewVersionOf","IsPreviousVersionOf");
        ret.put("IsReferencedBy","References");
        ret.put("IsSupplementTo","IsSupplementedBy");
        ret.put("IsCitedBy","Cites");
        ret.put("IsCompiledBy","Compiles");
        ret.put("IsDocumentedBy","Documents");
        ret.put("IsPartOf","HasPart");
        ret.put("IsPreviousVersionOf","IsNewVersionOf");
        ret.put("References","IsReferencedBy");
        ret.put("IsSupplementedBy","IsSupplementTo");
        return ret;
    }

    @Override
    public void end(Context ctx) throws Exception {
    }

    @Override
    public void finish(Context ctx) throws Exception {
        // nothing to do
    }

    protected HttpSolrServer getSolr()
    {
        if (solr == null)
        {
            String solrService = "http://localhost:8081/solr/search";
            String solrServiceFromConfig = ConfigurationManager.getProperty("discovery", "search.server");

            log.debug("Got Solr URL from config: " + solrServiceFromConfig);

            UrlValidator urlValidator = new UrlValidator(
                    UrlValidator.ALLOW_LOCAL_URLS);
            if (urlValidator.isValid(solrService))
            {
                try
                {
                    log.debug("Solr URL: " + solrService);
                    solr = new HttpSolrServer(solrService);

                    solr.setBaseURL(solrService);
                    solr.setUseMultiPartPost(true);
                    // Dummy/test query to search for Item (type=2) of ID=1
                    SolrQuery solrQuery = new SolrQuery()
                            .setQuery("*:*");
                    // Only return obj identifier fields in result doc
                    //solrQuery.setFields(RESOURCE_RESOURCETYPE_FIELD,
                    //        RESOURCE_ID_FIELD);
                    QueryResponse resp = solr.query(solrQuery);
                    log.debug("Solr test query done - got "+Long.toString(resp.getResults().getNumFound())+" results!");
                }
                catch (SolrServerException e)
                {
                    log.error("Error while initializing solr server", e);
                }
            }
            else
            {
                log.error("Error while initializing solr, invalid url: "
                        + solrService);
            }
        }

        return solr;
    }

}
