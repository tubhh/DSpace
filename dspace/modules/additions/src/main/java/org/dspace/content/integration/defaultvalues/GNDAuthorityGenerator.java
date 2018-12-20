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
import org.dspace.app.cris.model.ResearcherPage;
import org.dspace.app.cris.util.ResearcherPageUtils;
import org.dspace.content.Metadatum;
import org.dspace.content.authority.Choices;
import org.dspace.content.Item;
import org.dspace.core.Constants;

import org.apache.log4j.Logger;

public class GNDAuthorityGenerator implements EnhancedValuesGenerator
{

    private ApplicationService applicationService;
    protected String enhancingRole;

    private static final Logger log = Logger
            .getLogger(GNDAuthorityGenerator.class);

    public void setApplicationService(ApplicationService applicationService){
        this.applicationService = applicationService;
    }

    public void setEnhancingRole(String role){
        this.enhancingRole = role;
    }

    @Override
    public DefaultValuesBean generateValues(Item item, String schema,
            String element, String qualifier, String value)
    {
log.debug("Starting to enhance item "+item.getID()+"  (Handle "+item.getHandle()+") on "+schema+"."+element+"."+qualifier+" with value "+value);
        DefaultValuesBean result = new DefaultValuesBean();
        result.setLanguage("en");
        result.setMetadataSchema(schema);
        result.setMetadataElement(element);
        result.setMetadataQualifier(qualifier);
        Metadatum[] m = this.enhanceRole(item);
        int num = m.length;
        String[] values = new String[num];
        String[] authorities = new String[num];
        int[] confidences = new int[num];
log.debug("Found "+num+" "+enhancingRole+" elements");
        for (int idx = 0; idx < num; idx++){
log.debug("Found "+enhancingRole+" element "+m[idx].value+" at Item ID "+item.getID()+" (Handle "+item.getHandle()+")");
    	if (StringUtils.isNotEmpty(m[idx].authority)){
log.debug("Item ID "+item.getID()+" (Handle "+item.getHandle()+") has authorityKey "+m[idx].authority);
    	    ResearcherPage rp = applicationService
                        .getResearcherByAuthorityKey(m[idx].authority);
            if (rp != null) {
    	        String gndid = ResearcherPageUtils.getStringValue(rp, "gndid");
                if (StringUtils.isNotBlank(gndid)) {
log.debug("Found GND ID "+gndid+" for "+enhancingRole+" "+m[idx].value);
                    values[idx] = m[idx].value;
    		    authorities[idx]=gndid;
    		    confidences[idx]=Choices.CF_ACCEPTED;
    	        }
    	        else {
                    values[idx] = m[idx].value;
    		    authorities[idx]="";
    		    confidences[idx]=Choices.CF_UNSET;
    	        }
            }
    	    else {
                values[idx] = m[idx].value;
    		authorities[idx]="";
    		confidences[idx]=Choices.CF_UNSET;
    	    }
    	}
    	else {
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

    protected Metadatum[] enhanceRole(Item item) {
        Metadatum[] m;
        if (enhancingRole.equals("author")) {
            m = item.getDC("contributor", "author", Item.ANY);
        }
        else if (enhancingRole.equals("advisor")) {
            m = item.getMetadata("tuhh", "contributor", "advisor", Item.ANY);
        }
        else {
            m = item.getDC("contributor", "author", Item.ANY);
        }

        return m;
    }

}
