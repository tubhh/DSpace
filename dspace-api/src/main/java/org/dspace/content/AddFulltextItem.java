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
import org.dspace.eperson.EPerson;

import java.io.IOException;
import java.sql.SQLException;

public class AddFulltextItem implements InProgressSubmission {

	private static Logger log = Logger.getLogger(AddFulltextItem.class);

	private Item item;

	private Collection fulltextCollection;

	/**
	 * Legacy 'InProgressSubmission' constructor - for our 'Add Files' purposes, this should not be used
	 * @param item
	 */
	public AddFulltextItem(Item item) {
		this.item = item;
		try {
			this.fulltextCollection = item.getParentObject();
		} catch(SQLException e) {
			log.error("Error setting fulltext collection: " + e.getMessage());
			throw new RuntimeException(e);
		}
	}

	/**
	 * Constructor that includes collection, avoiding the need to use contextual DB queries from this object
	 * @param item
	 * @param collection
	 */
	public AddFulltextItem(Item item, Collection collection) {
		this.item = item;
		if (collection == null) {
			try {
				this.fulltextCollection = item.getParentObject();
			} catch(SQLException e) {
				log.error("Error setting fulltext collection: " + e.getMessage());
				throw new RuntimeException(e);
			}
		} else {
			this.fulltextCollection = collection;
		}
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
		return this.fulltextCollection;
	}

	@Override
	public EPerson getSubmitter() throws SQLException {
		return item.getSubmitter();
	}

	// New method to allow setting of
	public void setSubmitter(EPerson submitter) throws SQLException {
		item.setSubmitter(submitter);
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
