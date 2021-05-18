package org.dspace.submit.lookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.util.ResearcherPageUtils;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverResult;
import org.dspace.discovery.SearchService;

import gr.ekt.bte.core.AbstractModifier;
import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.StringValue;
import gr.ekt.bte.core.Value;

/**
 * Class that abstracts the behavior of {@link AuthorityLookupModifier}
 * to be able to alter the logic
 *
 * @param <T>
 */
public abstract class AbstractLookupModifier<T extends ACrisObject> extends AbstractModifier {

	public AbstractLookupModifier(String string) 
	{
		super(string);
	}
	
	private static final Logger log = Logger.getLogger(AbstractLookupModifier.class);

    /**
     * the key is the BTE field, the value the SOLR field
     */
    private Map<String, String> metadataInputConfiguration;

    /**
     * the key is the CRIS object prop name, the value the BTE field
     */
    private Map<String, String> mappingOutputConfiguration;

	// the list of BTE field that should be linked to the CRIS object if found via
	// the authority framework. They need to appear in the enhanced fields map as
	// well
    private List<String> mappingAuthorityConfiguration;

    private Integer resourceTypeID;

    private SearchService searchService;

    private Class<T> clazzCrisObject;
	
	@Override
    public Record modify(MutableRecord rec)
    {
        try
        {
        	List<T> crisObjects = new ArrayList<T>();
        	List<Integer> crisConfidence = new ArrayList<Integer>();
        	
            for (String mm : metadataInputConfiguration.keySet())
            {
            	// lookup for the cris object using the preferred field
                List<String> values = new ArrayList<String>();
                values.addAll(normalize(getValue(rec, mm)));
                List<String> extraVals = getExtraValues(rec);
                
                int pos = 0;
                for (String value : values)
                {
					// if we have already identified the cris object using another field is not
					// necessary to make a second lookup
                	if (crisObjects.size() > pos && crisObjects.get(pos) != null) {
                		continue;
                	}
                    if (StringUtils.isNotBlank(value))
                    {
                        DiscoverQuery query = new DiscoverQuery();
                        query.setQuery(getQuery(value, metadataInputConfiguration.get(mm), resourceTypeID, extraVals, pos));
                        DiscoverResult result = searchService.search(null,
                                query, true);
                        
                        processResults(result, crisObjects, extraVals, crisConfidence, pos);
                    }
                    pos++;                    
                }
            }
            
            int pos = 0;
            for (T cris : crisObjects) {
				if (cris != null) {
					// only process the additional information if a CRIS object has been found
					for (String propShortname : mappingOutputConfiguration.keySet()) {
						String bteField = mappingOutputConfiguration.get(propShortname);
						List<Value> exValues = rec.getValues(bteField);
						List<Value> newValues = new ArrayList<Value>();
						rec.removeField(bteField);
						
						// add back all the existing values as is 
						for (int iPos = 0; iPos < pos; iPos++) {
							newValues.add(exValues.size() > iPos ? exValues.get(iPos) : new StringValue(""));
						}

						if (ResearcherPageUtils.getStringValue(cris, propShortname) != null) {
							if (mappingAuthorityConfiguration.contains(bteField)) {
								newValues.add(new StringValue(ResearcherPageUtils.getStringValue(cris, propShortname)
										+ SubmissionLookupService.SEPARATOR_VALUE_REGEX + cris.getCrisID()
										+ SubmissionLookupService.SEPARATOR_VALUE_REGEX + crisConfidence.get(pos)));
							} else {
								newValues.add(new StringValue(ResearcherPageUtils.getStringValue(cris, propShortname)));
							}
						}
						else {
							newValues.add(new StringValue(""));
						}
						
						// add back all the existing values not yet processed
						for (int iPos = pos + 1; iPos < exValues.size(); iPos++) {
							newValues.add(exValues.get(iPos));
						}
						
						rec.addField(bteField, newValues);
					}
				}
            	pos++;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return null;
        }
        return rec;
    }
	
	protected List<String> getValue(MutableRecord rec, String md)
    {
        List<String> result = new ArrayList<String>();
        if (StringUtils.isNotBlank(md))
        {
            List<Value> vals = rec.getValues(md);
            if (vals != null && vals.size() > 0)
            {
                for (Value val : vals)
                {
                    result.add(val.getAsString());
                }
            }
        }

        return result;
    }

    public List<String> normalize(List<String> values)
    {
        // override this method to perform any normalization
        return values;
    }
    
    protected List<String> getExtraValues(MutableRecord rec)
    {
    	// override this method to retrieve any extra values to check
    	return null;
    }
    
    protected String getQuery(String value, String field, Integer resID, List<String> extraVs, Integer pos)
    {
    	// override this method to specify the solr query
    	return null;
    }
    
    protected void processResults(DiscoverResult result, List<T> crisObjects, List<String> extraVs, List<Integer> crisConfidence, Integer pos)
    {
    	// override this method to process the objects to match
    }
    

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setMetadataInputConfiguration(
            Map<String, String> metadataInputConfiguration)
    {
        this.metadataInputConfiguration = metadataInputConfiguration;
    }

    public void setMappingOutputConfiguration(
            Map<String, String> mappingOutputConfiguration)
    {
        this.mappingOutputConfiguration = mappingOutputConfiguration;
    }

    public void setMappingAuthorityConfiguration(
            List<String> mappingAuthorityConfiguration)
    {
        this.mappingAuthorityConfiguration = mappingAuthorityConfiguration;
    }

    public void setResourceTypeID(Integer resourceTypeID)
    {
        this.resourceTypeID = resourceTypeID;
    }

    public Class<T> getClazzCrisObject()
    {
        return clazzCrisObject;
    }

    public void setClazzCrisObject(Class<T> clazzCrisObject)
    {
        this.clazzCrisObject = clazzCrisObject;
    }

}
