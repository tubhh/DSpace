/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.integration.defaultvalues;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.dspace.app.cris.service.ApplicationService;
import org.dspace.app.cris.model.Project;
import org.dspace.app.cris.util.ResearcherPageUtils;
import org.dspace.content.Metadatum;
import org.dspace.content.authority.Choices;
import org.dspace.content.Item;
import org.dspace.core.Constants;

import org.apache.log4j.Logger;

public class TypeMapper implements EnhancedValuesGenerator
{

    private ApplicationService applicationService;
    private static final Logger log = Logger
            .getLogger(TypeMapper.class);

    public void setApplicationService(ApplicationService applicationService){
        this.applicationService = applicationService;
    }

    @Override
    public DefaultValuesBean generateValues(Item item, String schema,
            String element, String qualifier, String value)
    {
        DefaultValuesBean result = new DefaultValuesBean();
        result.setLanguage("en");
        result.setMetadataSchema(schema);
        result.setMetadataElement(element);
        result.setMetadataQualifier(qualifier);
        Metadatum[] m = item.getMetadata("dc", "type", null, Item.ANY);
        Metadatum[] n = item.getMetadata("dc", "type", "thesis", Item.ANY);
        String[] v = new String[1];
        if (n.length > 0) {
            v[0] = n[0].value;
        }
        else if (m.length > 0) {
            v[0] = m[0].value;
        }
        if (v[0] != null) {
            result.setValues(v);
            return result;
        }

        return null;
    }

}
