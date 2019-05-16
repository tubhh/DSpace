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

        String targetschema = ConfigurationManager.getProperty("cris", "relationsetter.localschema");
        String sourceschema = ConfigurationManager.getProperty("cris", "relationsetter.sourceschema");
//# Reciprocal field definitions are taken from dspace.cfg option ItemAuthority.reciprocalMetadata.<fieldName>

        DSpaceObject dso = event.getSubject(ctx);
        //MetadataValue.PARENT_PLACEHOLDER_VALUE

        // Unless this is  an item, we'll skip this item
        if (dso.getType() == Constants.ITEM)
        {
            Item item = (Item)dso;
            Map<String, String> reciprocalRelations = getReciprocalRelationsList();
            Metadatum[] relations = item.getMetadata(sourceschema, "relation", Item.ANY, Item.ANY);
            // Walk through datacite relations and check contents
            if (relations.length > 0) {
                for (Metadatum relation : relations) {
                    try {
                        // Cut unwanted identifier type prefix
                        if (prefixIsSupported(relation.value.substring(0,3))) {
                            String identifier = relation.value.substring(4).trim();
                            log.info("Checking local items for ID "+identifier);
                            Item relatedItem = null;
                            // Search for identifier
                            String query = buildQuery(identifier);
                            SolrQuery solrQuery = new SolrQuery()
                                .setQuery(query);
                            solrQuery.setFields("search.resourceid");
                            QueryResponse resp = getSolr().query(solrQuery);
                            logDebugMessage("Looking for identifier "+identifier+" in Solr... Query was: "+query+". Got "+Long.toString(resp.getResults().getNumFound())+" results.");
                            if (resp.getResults().getNumFound() > 0) {
                                SolrDocumentList resultList = resp.getResults();
                                for (SolrDocument result : resultList) {
                                    int internalId = (int)result.getFieldValue("search.resourceid");
                                    relatedItem = Item.find(ctx,internalId);
                                    logDebugMessage("Found related item for "+getItemHandle(item)+" while looking for identifier "+identifier+": "+getItemHandle(relatedItem));
                                }
                                if (relatedItem != null) {
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
                                    item.clearMetadata(targetschema, "relation", relation.qualifier, Item.ANY);
                                    // transfer content to a corresponding field in local schema
                                    item.addMetadata(targetschema, "relation", relation.qualifier, lang, title, getItemHandle(relatedItem), 600);
                                    // build corresponding datacite field in related item
                                    // only set this field if the item is already approved and not in workflow and the metadata value has not been set
                                    Metadatum[] relationMd = relatedItem.getMetadata(sourceschema, "relation", reciprocalRelations.get(relation.qualifier), Item.ANY);
                                    boolean applied = false;
                                    for (Metadatum relMd : relationMd) {
                                        if (relMd.value.equals(id)) {
                                            applied = true;
                                        }
                                    }
                                    if (item.isArchived() && applied == false) {
                                        relatedItem.clearMetadata(sourceschema, "relation", reciprocalRelations.get(relation.qualifier), Item.ANY);
                                        relatedItem.addMetadata(sourceschema, "relation", reciprocalRelations.get(relation.qualifier), null, id, null, -1);
                                        relatedItem.updateMetadata();
                                        relatedItem.update();
                                    }
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
    }

    protected static boolean prefixIsSupported(String item) {
        String prefixesToCheck = ConfigurationManager.getProperty("cris", "relationsetter.identifier.prefixes");
        String[] supportedPrefixes = prefixesToCheck.split(",");
        for (String n : supportedPrefixes) {
            if (item.equals(n)) {
                return true;
            }
        }
        return false;
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

    /**
     * Internal utitity method to get a description of the handle
     *
     * @param item The item to get a description of
     * @return The handle, or in workflow
     */
    private static String buildQuery(String identifier)
    {
        //relationsetter.indexfields = dc.identifier.doi;handle;dc.identifier.urn.-
        String indexfields = ConfigurationManager.getProperty("cris", "relationsetter.indexfields");
        String q = null;
        int ind = 0;
        String[] fieldsToQuery = indexfields.split(";");
        for (String f : fieldsToQuery) {
            if (q == null) {
                q = f+":"+identifier;
            }
            else {
                q = q+f+":"+identifier;
            }
            ind++;
            if (ind < fieldsToQuery.length) {
                q = q+" OR ";
            }
        }
        return q;
    }

    /**
     * Built in reciprocal field list
     *
     * @return Map of reciprocal fields
     */
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

    private static Map<String, String> getReciprocalRelationsListFromConfig() {
//# Reciprocal field definitions are taken from dspace.cfg option ItemAuthority.reciprocalMetadata.<fieldName>
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
            String solrService = ConfigurationManager.getProperty("discovery", "search.server");

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
                    logDebugMessage("Solr test query done - got "+Long.toString(resp.getResults().getNumFound())+" results!");
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
