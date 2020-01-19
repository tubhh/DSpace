/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.handle.HandleManager;
import org.mockito.internal.matchers.Null;

import java.io.IOException;
import java.sql.SQLException;

public class AddFulltextItem extends EditItem {

	private static Logger log = Logger.getLogger(AddFulltextItem.class);

	private Item item;

	public AddFulltextItem(Item item) {
		super(item);
		this.item = item;
	}

	@Override
	public int getID() {
		return item.getID();
	}

	@Override
	public void deleteWrapper() throws SQLException, IOException, AuthorizeException {
		// nothing to delete
		return;
	}

	@Override
	public void update() throws SQLException, AuthorizeException {
		item.update();
	}

	@Override
	public Item getItem() {
		return item;
	}

	@Override
	public Collection getCollection() {

		// Get the "Fulltext Collection" that will become the "Submitted to" collection here
		String collectionHandle = ConfigurationManager.getProperty("submit.fulltext.to-collection");

		try {
			DSpaceObject dso = HandleManager.resolveToObject(new Context(), collectionHandle);
			if (dso.getType() == Constants.getTypeID("COLLECTION")) {
				Collection collection = (Collection) dso;
				return collection;
			} else {
				log.error("Handle is not of type collection " + collectionHandle);
				throw new RuntimeException();
			}
		} catch (SQLException e) {
			throw new RuntimeException();
		} catch (NullPointerException e) {
			log.error("Collection not found (NPE) : " + collectionHandle);
			throw new RuntimeException();
		}
		// In EditItem, this would be the item parentObject
		// But in this case, we should make it hte configured FullText collection
		/*
		try {
			return item.getParentObject();
		} catch (SQLException e) {
			throw new RuntimeException();
		}
		 */
	}

	@Override
	public EPerson getSubmitter() throws SQLException {
		return item.getSubmitter();
	}

	@Override
	public boolean hasMultipleFiles() {
		return true;
	}

	@Override
	public void setMultipleFiles(boolean b) {
	}

	@Override
	public boolean hasMultipleTitles() {
		return true;
	}

	@Override
	public void setMultipleTitles(boolean b) {
	}

	@Override
	public boolean isPublishedBefore() {
		return true;
	}

	@Override
	public void setPublishedBefore(boolean b) {
	}

}
