/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Utils;
import org.dspace.content.Metadatum;

public class ResolverDisplayStrategy extends AUniformDisplayStrategy
{
    /** log4j category */
    private static Logger log = Logger.getLogger(ResolverDisplayStrategy.class);
    
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
                    if (value.startsWith(checkUrn))
                    {
                        foundUrn = checkUrn;
                    }
                }
            }

            if (foundUrn != null)
            {

                if (value.startsWith(foundUrn + ":") || field.endsWith(foundUrn))
                {
                    value = value.substring(foundUrn.length() + 1).trim();
                }

                url = urn2baseurl.get(foundUrn);
            }
        }

        String startLink = null;
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
}
