package org.dspace.identifier.doi;

import java.util.HashMap;
import java.util.Map;

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
			DOIIdentifierProvider doiIdentifierService = new DSpace().getSingletonService(DOIIdentifierProvider.class);
			
			for (Map.Entry<Integer, Item> itemEntry : itemIDs.entrySet())
			{
				try
				{
					doiIdentifierService.register(ctx, itemEntry.getValue());
				} catch (IdentifierException e)
				{
					throw new RuntimeException("Can't create an Identifier!", e);
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