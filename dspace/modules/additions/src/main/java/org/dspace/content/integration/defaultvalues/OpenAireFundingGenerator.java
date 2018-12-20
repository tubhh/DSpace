/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.integration.defaultvalues;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.dspace.app.cris.service.ApplicationService;
import org.dspace.app.cris.model.Project;
import org.dspace.app.cris.util.ResearcherPageUtils;
import org.dspace.content.Metadatum;
import org.dspace.content.authority.Choices;
import org.dspace.content.Item;
import org.dspace.core.Constants;

import org.apache.log4j.Logger;

public class OpenAireFundingGenerator implements EnhancedValuesGenerator
{

    private ApplicationService applicationService;
    private static final Logger log = Logger
            .getLogger(OpenAireFundingGenerator.class);

    public void setApplicationService(ApplicationService applicationService){
        this.applicationService = applicationService;
    }

    @Override
    public DefaultValuesBean generateValues(Item item, String schema,
            String element, String qualifier, String value)
    {
        DefaultValuesBean result = new DefaultValuesBean();
        result.setLanguage("en");
        result.setMetadataSchema(schema);
        result.setMetadataElement(element);
        result.setMetadataQualifier(qualifier);
        Metadatum[] m = item.getMetadata("dc", "relation", "project", Item.ANY);
        int num = m.length;
        String[] values = new String[num];
        String[] authorities = new String[num];
        int[] confidences = new int[num];
        for (int idx = 0; idx < num; idx++){
log.debug("Found project relation "+m[idx].value+" at Item ID "+item.getID()+" (Handle "+item.getHandle()+")");
    	if (StringUtils.isNotEmpty(m[idx].authority)){
log.debug("project relation has authorityKey "+m[idx].authority);
    	    Project ro = applicationService
                        .getEntityByCrisId(m[idx].authority);
            if (ro != null) {
    	        String openairefundinginformation = ResearcherPageUtils.getStringValue(ro, "openairefundinginformation");
                if (StringUtils.isNotBlank(openairefundinginformation)) {
log.debug("Found Funding information for OpenAire "+openairefundinginformation);
                    values[idx] = openairefundinginformation;
    		    authorities[idx]=m[idx].authority;
    		    confidences[idx]=Choices.CF_ACCEPTED;
    	        }
    	        else {
log.debug("No Funding information for OpenAire set for this project ");
                    values[idx] = "";
    		    authorities[idx]="";
    		    confidences[idx]=Choices.CF_UNSET;
    	        }
            }
    	    else {
log.debug("No Funding information for OpenAire set for this project ");
                values[idx] = "";
    		authorities[idx]="";
    		confidences[idx]=Choices.CF_UNSET;
    	    }
    	}
    	else {
log.debug("Empty authority key for project "+m[idx].value);
                values[idx] = "";
		authorities[idx]="";
		confidences[idx]=Choices.CF_UNSET;
	    }
        }

        result.setAuthorities(authorities);
        result.setValues(values);
        result.setConfidences(confidences);
        return result;
    }

}
