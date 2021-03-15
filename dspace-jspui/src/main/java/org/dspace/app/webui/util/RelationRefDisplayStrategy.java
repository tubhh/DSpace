/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.util;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.commons.validator.routines.UrlValidator;

import org.apache.log4j.Logger;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Utils;
import org.dspace.content.Metadatum;
import org.dspace.content.Item;

public class RelationRefDisplayStrategy extends ResolverDisplayStrategy
{
    /** log4j category */
    private static Logger log = Logger.getLogger(RelationRefDisplayStrategy.class);

    private HttpSolrServer solr = null;

    /** Hashmap of urn base url resolver, from dspace.cfg */
    private static Map<String,String> urn2baseurl;
    
    private static final String HANDLE_DEFAULT_BASEURL = "http://hdl.handle.net/";

    private static final String DOI_DEFAULT_BASEURL = "https://doi.org/";
    
    private void init()
    {
        if (urn2baseurl != null)
            return;

        synchronized (ResolverDisplayStrategy.class)
        {
            if (urn2baseurl == null)
            {
                urn2baseurl = new HashMap<String, String>();
                String urn;
                for (int i = 1; null != (urn = ConfigurationManager.getProperty("webui.resolver."+i+".urn")); i++){
                    String baseurl = ConfigurationManager.getProperty("webui.resolver."+i+".baseurl"); 
                    if (baseurl != null){
                    urn2baseurl.put(ConfigurationManager
                            .getProperty("webui.resolver."+i+".urn"),
                            baseurl);
                    } else {
                        log.warn("Wrong webui.resolver configuration, you need to specify both webui.resolver.<n>.urn and webui.resolver.<n>.baseurl: missing baseurl for n = "+i);
                    }
                }
                
                // Set sensible default if no config is found for doi & handle
                if (!urn2baseurl.containsKey("doi")){
                    urn2baseurl.put("doi",DOI_DEFAULT_BASEURL);
                }
                
                if (!urn2baseurl.containsKey("hdl")){
                    urn2baseurl.put("hdl",HANDLE_DEFAULT_BASEURL);
                }
                
                if (!urn2baseurl.containsKey("mailto")){
                    urn2baseurl.put("mailto","mailto:");
                }
            }
        }
    }

    protected String getLinkDisplayForValue(HttpServletRequest hrq, String value, int itemid, String field)
    {
        init();
        String url = null;
        StringBuffer sb = new StringBuffer();
        
        if (value.startsWith("http://") || value.startsWith("https://")
                || value.startsWith("ftp://")
                || value.startsWith("ftps://"))
        {
            // Already a URL, print as if it was a regular link
        	url = value;
            value = "";
        }
        else
        {
            String foundUrn = null;
            if (getPluginInstanceName() != null && !getPluginInstanceName().equals("resolver"))
            {
                foundUrn = getPluginInstanceName();
            }
            else
            {
                for (String checkUrn : urn2baseurl.keySet())
                {
                    if (value.startsWith(checkUrn) || field.endsWith(checkUrn))
                    {
                        foundUrn = checkUrn;
                    }
                }
            }

            if (foundUrn != null)
            {

                if (value.startsWith(foundUrn + ":"))
                {
                    value = value.substring(foundUrn.length() + 1).trim();
                }
                if (field.endsWith(foundUrn)) {
                    value = value.trim();
                }

                url = urn2baseurl.get(foundUrn);
            }
        }

        String relatedItemTitle = "";
        try {
            Item relatedItem = null;
            // Search for identifier
            String query = buildQuery(value);
            if (query != null) {
            SolrQuery solrQuery = new SolrQuery()
                .setQuery(query);
            solrQuery.setFields("search.resourceid");
            QueryResponse resp = getSolr().query(solrQuery);
            log.debug("Looking for identifier "+value+" in Solr... Query was: "+query+". Got "+Long.toString(resp.getResults().getNumFound())+" results.");
            if (resp.getResults().getNumFound() > 0) {
                SolrDocumentList resultList = resp.getResults();
                for (SolrDocument result : resultList) {
                    int internalId = (int)result.getFieldValue("search.resourceid");
                    relatedItem = Item.find(UIUtil.obtainContext(hrq),internalId);
                }
                if (relatedItem != null) {
                    relatedItemTitle = relatedItem.getMetadata("dc.title");
                }
            }
            }
        }
        catch (SQLException e)
        {
            log.error(e.getMessage(), e);
        }
        catch (SolrServerException e)
        {
            log.error("Error while initializing solr server", e);
        }

        String internalLinkIcon = ConfigurationManager.getProperty("cris", "relation.internallink_faclass");
        String externalLinkIcon = ConfigurationManager.getProperty("cris", "relation.externallink_faclass");
        if (internalLinkIcon == null) {
            internalLinkIcon = "anchor";
        }
        if (externalLinkIcon == null) {
            externalLinkIcon = "link";
        }

        String startLink = null;
        String linkIcon = null;
        if (!relatedItemTitle.equals("")) {
            linkIcon = " <span class=\"fa fa-"+internalLinkIcon+"\"";
            linkIcon += " title=\""+relatedItemTitle+"\"";
            linkIcon += "> </span>";
        } else {
            linkIcon = " <span class=\"fa fa-"+externalLinkIcon+"\"> </span>";
        }
        try
        {   boolean newwindow=ConfigurationManager.getBooleanProperty("webui.resolver.link.newwindow");
            if (newwindow){
                startLink = "<a target=_blank href=\"" + url + URLEncoder.encode(value, "UTF-8") + "\">";
            } else {
                startLink = "<a href=\"" + url + URLEncoder.encode(value, "UTF-8") + "\">";
            }
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
        String endLink = "</a>";
        sb.append(url != null?startLink:"");
        sb.append(Utils.addEntities(value!=""?value:url));
        sb.append(linkIcon);
        sb.append(url != null?endLink:"");
        return sb.toString();
    }

    public String getMetadataDisplay(HttpServletRequest hrq, int limit,
            boolean viewFull, String browseType, int colIdx, int itemid, String field,
            Metadatum[] metadataArray, boolean disableCrossLinks, boolean emph)
    {
        String metadata;
        // limit the number of records if this is the author field (if
        // -1, then the limit is the full list)
        boolean truncated = false;
        int loopLimit = metadataArray.length;
        if (limit != -1)
        {
            loopLimit = (limit > metadataArray.length ? metadataArray.length
                    : limit);
            truncated = (limit < metadataArray.length);
            log.debug("Limiting output of field " + field + " to "
                    + Integer.toString(loopLimit) + " from an original "
                    + Integer.toString(metadataArray.length));
        }

        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < loopLimit; j++)
        {
            sb.append(getLinkDisplayForValue(hrq, metadataArray[j].value, itemid, field));
            if (j < (loopLimit - 1))
            {
                if (colIdx != -1) // we are showing metadata in a table row
                                  // (browse or item list)
                {
                    sb.append("; ");
                }
                else
                {
                    // we are in the item tag
                    sb.append("<br />");
                }
            }
        }
        if (truncated)
        {
            if (colIdx != -1)
            {
                sb.append("; ...");
            }
            else
            {
                sb.append("<br />...");
            }
        }

        if (colIdx != -1) // we are showing metadata in a table row (browse or
                          // item list)
        {
            metadata = (emph ? "<strong><em>" : "<em>") + sb.toString()
                    + (emph ? "</em></strong>" : "</em>");
        }
        else
        {
            // we are in the item tag
            metadata = (emph ? "<strong>" : "") + sb.toString()
                    + (emph ? "</strong>" : "");
        }
        
        return metadata;
    }

    protected String getDisplayForValue(HttpServletRequest hrq, String value, int itemid)
    {
        return null;
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
        if (identifier.equals("")) {
            return null;
        }
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
