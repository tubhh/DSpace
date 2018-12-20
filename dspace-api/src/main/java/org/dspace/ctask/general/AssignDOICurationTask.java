package org.dspace.ctask.general;

import java.io.IOException;
import java.sql.SQLException;

import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;
import org.dspace.identifier.DOIIdentifierProvider;
import org.dspace.identifier.IdentifierException;
import org.dspace.utils.DSpace;

public class AssignDOICurationTask extends AbstractCurationTask {
	
	@Override
	public int perform(DSpaceObject dso) throws IOException {
		distribute(dso);
		return Curator.CURATE_SUCCESS;
	}
	
	@Override
	protected void performItem(Item item) throws SQLException, IOException {
		Context context = Curator.curationContext();
		DOIIdentifierProvider doiIdentifierService = new DSpace().getSingletonService(DOIIdentifierProvider.class);
        try {
        	doiIdentifierService.register(context, item);
        } catch (IdentifierException e) {
            throw new RuntimeException("Can't create an Identifier!", e);
        }
	}

}