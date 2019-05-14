/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.integration;

public class DOExtraMetadataGenerator
    extends CRISExtraBasicMetadataGenerator
{
    private String type;

    public String getType()
    {
        return type;
    }

    public void setType(String newInstance)
    {
        this.type = newInstance;
    }
    
}
