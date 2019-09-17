/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.configuration;

import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;

public interface SecurityCheck {

    boolean isAuthorized(Context context, DSpaceObject dso);

}
