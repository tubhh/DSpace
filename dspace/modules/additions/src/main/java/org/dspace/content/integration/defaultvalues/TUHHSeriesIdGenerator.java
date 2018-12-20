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
import org.dspace.app.cris.model.ResearchObject;
import org.dspace.app.cris.util.ResearcherPageUtils;
import org.dspace.content.Metadatum;
import org.dspace.content.authority.Choices;
import org.dspace.content.Item;
import org.dspace.core.Constants;

import org.apache.log4j.Logger;

public class TUHHSeriesIdGenerator implements EnhancedValuesGenerator
{

    private ApplicationService applicationService;
    private static final Logger log = Logger
            .getLogger(TUHHSeriesIdGenerator.class);

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
        Metadatum[] m = item.getMetadata("tuhh", "relation", "ispartofseries", Item.ANY);
        int num = m.length;
        String[] values = new String[num];
        String[] authorities = new String[num];
        int[] confidences = new int[num];
        for (int idx = 0; idx < num; idx++){
log.debug("Found ispartof relation "+m[idx].value+" at Item ID "+item.getID()+" (Handle "+item.getHandle()+")");
    	if (StringUtils.isNotEmpty(m[idx].authority)){
log.debug("ispartof relation has authorityKey "+m[idx].authority);
    	    ResearchObject ro = applicationService
                        .getEntityByCrisId(m[idx].authority);
            if (ro != null) {
    	        String tuhhseriesid = ResearcherPageUtils.getStringValue(ro, "journalstuhhseriesid");
                if (StringUtils.isNotBlank(tuhhseriesid)) {
log.debug("Found TUHH Series ID "+tuhhseriesid+" for Journal "+m[idx].value);
                    values[idx] = m[idx].value;
    		    authorities[idx]=tuhhseriesid;
    		    confidences[idx]=Choices.CF_ACCEPTED;
    	        }
    	        else {
log.debug("No TUHH Series ID "+tuhhseriesid+" set for Journal "+m[idx].value);
                    values[idx] = m[idx].value;
    		    authorities[idx]="";
    		    confidences[idx]=Choices.CF_UNSET;
    	        }
            }
    	    else {
log.debug("No CRIS entity found for Journal "+m[idx].value);
                values[idx] = m[idx].value;
    		authorities[idx]="";
    		confidences[idx]=Choices.CF_UNSET;
    	    }
    	}
    	else {
log.debug("Empty authority key for Journal "+m[idx].value);
                values[idx] = m[idx].value;
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
