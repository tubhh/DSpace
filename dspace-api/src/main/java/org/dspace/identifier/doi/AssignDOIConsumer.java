package org.dspace.identifier.doi;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.dspace.identifier.DOIIdentifierProvider;
import org.dspace.identifier.IdentifierException;
import org.dspace.utils.DSpace;

/**
 * Consumer used to internally register items without DOIs when they are moved between collections
 */
public class AssignDOIConsumer implements Consumer {
	
    /** log4j logger */
    private static Logger log = Logger.getLogger(AssignDOIConsumer.class);
    
	// items to be updated with DOI
	private Map<Integer, Item> itemIDs = null;
    
	@Override
	public void initialize() throws Exception {
	}

	/**
	 * Store a list of all the items that need to have their DOI registered
	 */
	@Override
	public void consume(Context ctx, Event event) throws Exception {
		int subjectType = event.getSubjectType();
        int eventType = event.getEventType();
        int objectType = event.getObjectType();
        
        if (itemIDs == null)
        {
        	itemIDs = new HashMap<Integer, Item>();
        }
        
        if (subjectType == Constants.COLLECTION && eventType == Event.ADD && objectType == Constants.ITEM)
        {
        	// put Item ID and Item into the created map
        	Item item = (Item)event.getObject(ctx);
        	int itemID = item.getID();
        	if (item.isArchived() && !itemIDs.containsKey(itemID))
        	{
        		itemIDs.put(itemID, item);
        	}
        }
	}

	/**
	 * Register DOIs for all items to work
	 */
	@Override
	public void end(Context ctx) throws Exception {
		if (itemIDs != null && itemIDs.size() > 0)
		{
			DOIIdentifierProvider provider = new DSpace().getSingletonService(DOIIdentifierProvider.class);
			
			for (Map.Entry<Integer, Item> itemEntry : itemIDs.entrySet())
			{
			    Item item = itemEntry.getValue();
			    
			    String doi = null;
				try
				{
				    doi = provider.register(ctx, item);
				} catch (IdentifierException e)
				{
					log.warn("AssignDOIConsumer can't create an Identifier!", e);
				}
				
				//try to force update metadata, to move doi in the correct status especially for DOI already registered 
				if(StringUtils.isNotBlank(doi)) {
    		        try
    		        {
    		            provider.updateMetadata(ctx, item, doi);
    		        }
    		        catch (IllegalArgumentException ex)
    		        {
    		            // should not happen, as we got the DOI from the DOIProvider
    		            log.warn("AssignDOIConsumer caught an IdentifierException.", ex);
    		        }
    		        catch (IdentifierException ex)
    		        {
    		            log.warn("AssignDOIConsumer cannot update metadata for Item with ID "
    		                    + item.getID() + " and DOI " + doi + ".", ex);
    		        }
				}
			}
			
			// browse updates wrote to the DB, so we have to commit.
			ctx.getDBConnection().commit();
		}
		itemIDs = null;
	}

	@Override
	public void finish(Context ctx) throws Exception {
	}

}