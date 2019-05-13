/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.integration;

import java.util.List;
import java.util.Map;

public class CRISExtraConfiguration
{
    private Map<String, List<CRISExtraMetadataGenerator>> extraMap;

    public void setExtraMap(Map<String, List<CRISExtraMetadataGenerator>> extraMap)
    {
        this.extraMap = extraMap;
    }

    public Map<String, List<CRISExtraMetadataGenerator>> getExtraMap()
    {
        return extraMap;
    }
}
