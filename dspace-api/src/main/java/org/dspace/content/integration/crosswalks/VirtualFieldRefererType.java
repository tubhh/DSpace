/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.integration.crosswalks;

import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.commons.lang3.StringUtils;
import org.dspace.content.Metadatum;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;

/**
 * Costruisce i singoli autori a partire dalla stringa allauthor
 * 
 * @author bollini
 */
public class VirtualFieldRefererType implements VirtualFieldDisseminator, VirtualFieldIngester
{
    private static Logger log = Logger
            .getLogger(VirtualFieldRefererType.class);

    public String[] getMetadata(Item item, Map<String, String> fieldCache, String fieldName)
    {
        String typeDC = "dc.type";
        if(ConfigurationManager.getProperty("crosswalk.virtualtype.value") != null)
            typeDC = ConfigurationManager.getProperty("crosswalk.virtualtype.value");

        Metadatum[] dcvs = item.getMetadataValueInDCFormat(typeDC);

        String[] virtualFieldName = fieldName.split("\\.");
        
        // virtualFieldName[0] == "virtual"
		String qualifier = virtualFieldName[2];

        String typeToMatch = "none";
        if (dcvs != null && dcvs.length > 0) {
            typeToMatch = dcvs[0].value;
        }

		String type = ConfigurationManager
				.getProperty("crosswalk.virtualname.referer.type." + qualifier + "." + typeToMatch);
                log.debug("Getting type for bibtex from dspace.cfg parameter crosswalk.virtualname.referer.type." + qualifier+ "." + typeToMatch);
		if (StringUtils.isNotBlank(type)) {
			return new String[] { type };
		}
        
		return new String[] { ConfigurationManager
				.getProperty("crosswalk.virtualname.referer.type." + qualifier) };
    }

    public boolean addMetadata(Item item, Map<String, String> fieldCache, String fieldName, String value)
    {
        // NOOP - we won't add any metadata yet, we'll pick it up when we finalise the item
        return true;
    }

    public boolean finalizeItem(Item item, Map<String, String> fieldCache)
    {
        return false;
    }
}
