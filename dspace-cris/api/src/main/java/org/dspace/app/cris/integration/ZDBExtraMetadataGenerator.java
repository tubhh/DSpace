/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.integration;

import java.util.Map;

import org.dspace.authority.AuthorityValue;

public interface ZDBExtraMetadataGenerator
{
    public Map<String, String> build(AuthorityValue val);
}
