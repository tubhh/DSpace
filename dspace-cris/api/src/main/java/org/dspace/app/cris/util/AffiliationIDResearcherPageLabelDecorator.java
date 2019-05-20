/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.util;

import org.apache.commons.lang.StringUtils;
import org.dspace.app.cris.model.ResearcherPage;
import org.dspace.app.cris.model.RestrictedField;
import org.dspace.app.cris.model.VisibilityConstants;

public class AffiliationIDResearcherPageLabelDecorator implements IResearcherPageLabelDecorator {

	@Override
	public String generateDisplayValue(String alternativeName, ResearcherPage rp) {
		 if (alternativeName.equals(rp.getFullName()))
	        {
			 	String dept = ResearcherPageUtils.getStringValue(rp, "dept");
			 	String gndid = ResearcherPageUtils.getStringValue(rp, "gndid");
			 	String orcid = ResearcherPageUtils.getStringValue(rp, "orcid");

			 	RestrictedField translatedName = rp.getTranslatedName();
			 	StringBuffer sb = new StringBuffer(rp.getFullName());
			 	if (translatedName != null
                        && translatedName.getValue() != null
                        && !translatedName.getValue().isEmpty()
                        && translatedName.getVisibility() == VisibilityConstants.PUBLIC) {
			 		sb.append(" " + translatedName.getValue());
			 	}

			 	sb.append(" (").append(rp.getCrisID()).append(")");

			 	if (StringUtils.isNotBlank(dept)) {
			 		sb.append(" - ").append(dept);
			 	}

			 	if (StringUtils.isNotBlank(gndid)) {
			 		sb.append(" - ").append(gndid);
			 	}
			 	
			 	if (StringUtils.isNotBlank(orcid)) {
			 		sb.append(" - ").append(orcid);
			 	}
			 	
	            return sb.toString();
	        }
	        else
	        {
	            return alternativeName + " See \"" + rp.getFullName() + "\" (" + rp.getCrisID() + ")";
	        }
	}

}
