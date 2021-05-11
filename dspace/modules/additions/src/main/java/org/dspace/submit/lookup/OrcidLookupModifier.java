package org.dspace.submit.lookup;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.content.DSpaceObject;
import org.dspace.content.authority.Choices;
import org.dspace.discovery.DiscoverResult;

import gr.ekt.bte.core.MutableRecord;

/**
 * Implementation of {@link AbstractLookupModifier} to use results from solr only if the orcid matches, 
 * if no orcid is found in the local or imported record then set confidence to uncertain
 *
 */
public class OrcidLookupModifier<T extends ACrisObject>
        extends AbstractLookupModifier<T>
{
    public OrcidLookupModifier()
    {
        super(OrcidLookupModifier.class.getSimpleName());
    }
    
    @Override
    protected List getExtraValues(MutableRecord rec) 
    {
    	List<String> orcid = new ArrayList<String>(normalize(getValue(rec, "orcid")));
        return orcid;
    }
    
    @Override
    protected String getQuery(String value, String field, Integer resID, List orcid, Integer pos) 
    {
    	return "search.resourcetype:" + resID
                + " AND (crisrp.orcid:\"" + orcid.get(pos) +"\"^50 OR " + field
                + ":\"" + value + "\")";
    }
    
    @Override
    protected void processResults(DiscoverResult result, List crisObjects, List orcid, List crisConfidence, Integer pos)
    {
    	T cris = null;
        boolean accepted = false;
        
        if (result.getTotalSearchResults() > 0) 
        {
        	String orcidString = (String)orcid.get(pos);
        	// if scopus contains no orcid then take the first and keep uncertain
        	if (StringUtils.isBlank(orcidString) || StringUtils.equals(ScopusUtils.PLACEHOLDER_NO_DATA, orcidString)) 
        	{
        		cris = (T) result.getDspaceObjects().get(0);
        	}
        	else {
        		for (DSpaceObject candidate : result.getDspaceObjects())
        		{
        			String orcidValue = candidate.getMetadata("orcid");
        			// if orcid is equal then take the candidate
        			if (StringUtils.equals(orcidValue, orcidString)) 
        			{
        				cris = (T) candidate;
        				accepted = true;
        				break;
        			}	// keep as uncertain candidate the first cris without orcid
        			else if (cris == null && StringUtils.isBlank(orcidValue))
        			{
        				cris = (T) candidate;
					}
        		}
        	}
        	
		}
        
        if (crisObjects.size() > pos) {
        	crisObjects.set(pos, cris);
        	crisConfidence.set(pos, accepted?Choices.CF_ACCEPTED:Choices.CF_UNCERTAIN);
        }
        else {
        	crisObjects.add(cris);
        	crisConfidence.add(accepted?Choices.CF_ACCEPTED:Choices.CF_UNCERTAIN);
        }
    }

}
