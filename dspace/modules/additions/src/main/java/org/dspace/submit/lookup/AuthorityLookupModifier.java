package org.dspace.submit.lookup;

import java.util.List;

import org.dspace.app.cris.model.ACrisObject;
import org.dspace.content.authority.Choices;
import org.dspace.discovery.DiscoverResult;


/**
 * This class enrich a BTE record with information extracted from a CRIS
 * authority. The metadataInput map contains the mapping between the information
 * potentially available in the incoming BTE record and the SOLR field to use to
 * lookup for the CRIS entity (i.e using the jissn field of the BTE make a solr
 * search on the crisjournal.journalissn field). When multiple inputs are
 * provided they will be used in the exact order and only the first match will
 * be returned
 * 
 * The mappingOutput map contains the information to add/override to/in the BTE
 * record if a CRIS entity is found. The mapptingAuthorityConfiguration contains
 * the list of BTE field to enrich with the CRIS ID as authority if the match is
 * found
 *
 */
public class AuthorityLookupModifier<T extends ACrisObject>
        extends AbstractLookupModifier<T>
{
    public AuthorityLookupModifier()
    {
        super(AbstractLookupModifier.class.getSimpleName());
    }
    
    protected String getQuery(String value, String field, Integer resID, List extraMd, Integer pos)
    {
    	return "search.resourcetype:" + resID
                + " AND " + field
                + ":\"" + value + "\"";
    }
    
    protected void processResults(DiscoverResult result, List crisObjects, List extraMd, List crisConfidence, Integer pos)
    {
    	T cris = null;
        if (result.getTotalSearchResults() == 1)
        {
            cris = (T) result.getDspaceObjects().get(0);
        }
        if (crisObjects.size() > pos) {
        	crisObjects.set(pos, cris);
        }
        else {
        	crisObjects.add(cris);
        }
        crisConfidence.set(pos, Choices.CF_ACCEPTED);
    }

}
