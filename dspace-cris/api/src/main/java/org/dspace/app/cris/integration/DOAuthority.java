/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.integration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dspace.app.cris.model.ResearchObject;
import org.dspace.core.ConfigurationManager;
import org.dspace.utils.DSpace;

public class DOAuthority extends CRISAuthority<ResearchObject>
{

    @Override
    public int getCRISTargetTypeID()
    {   
        return -1;
    }

    @Override
    public Class<ResearchObject> getCRISTargetClass()
    {
        return ResearchObject.class;
    }
    
    @Override
    public String getPublicPath() {
    	return null;
    }

	@Override
	public ResearchObject getNewCrisObject() {
		return new ResearchObject();
	}
	
    protected Map<String, String> getExtra(ResearchObject crisObject, String field) {
        Map<String, String> extras = new HashMap<String,String>();
        List<DOExtraMetadataGenerator> generators = new DSpace().getServiceManager().getServicesByType(DOExtraMetadataGenerator.class);
        if(generators!=null) {
            for(DOExtraMetadataGenerator gg : generators) {
                String anObject = ConfigurationManager.getProperty("cris.DOAuthority." +field+ ".new-instances");
                if(gg.getType().equals(anObject)) {
                    Map<String, String> extrasTmp = gg.build(crisObject);
                    extras.putAll(extrasTmp);
                }
            }
        }
        return extras;
    }

}
