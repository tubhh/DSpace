/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.integration.crosswalks;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Implements virtual field processing for generate a JSON  element to use with schema.org disseminator
 * 
 */
public class VirtualFieldSchemaOrgFunder implements VirtualFieldDisseminator,
        VirtualFieldIngester
{

    public String[] getMetadata(Item item, Map<String, String> fieldCache,
            String fieldName)
    {

        Context context = null;
        try
        {
            context = new Context();

            // Check to see if the virtual field is already in the cache
            // - processing is quite intensive, so we generate all the values on
            // first request
            if (fieldCache.containsKey(fieldName))
            {
                return new String[] { fieldCache.get(fieldName) };
            }
            
            Metadatum[] mds = item.getMetadata("dc", "description", "sponsorship",
                    Item.ANY);
            if (mds != null && mds.length > 0)
            {
                
                Metadatum md = mds[0];
                
                Map mmp = new LinkedHashMap<>();
                mmp.put("@type", "Organization");
                mmp.put("name", md.value);  
                if(StringUtils.isNotBlank(md.authority)) {
                    mmp.put("identifier", md.authority);
                }
                
                Gson gson = new GsonBuilder().create();
                String result = gson.toJson(mmp);
                fieldCache.put("virtual.funderschemaorg", result);
                // Return the value of the virtual field (if any)
                if (fieldCache.containsKey(fieldName))
                {
                    return new String[] { fieldCache.get(fieldName) };
                }
            }
        }
        catch (SQLException e)
        {
            // nothing
        }
        finally
        {
            if (context != null && context.isValid())
            {
                context.abort();
            }
        }
        return null;
    }

    public boolean addMetadata(Item item, Map<String, String> fieldCache,
            String fieldName, String value)
    {
        // NOOP - we won't add any metadata yet, we'll pick it up when we
        // finalise the item
        return true;
    }

    public boolean finalizeItem(Item item, Map<String, String> fieldCache)
    {
        return false;
    }
}
