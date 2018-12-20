/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.log4j.Logger;
import org.dspace.content.Metadatum;
import org.dspace.core.Utils;

public class HtmlDisplayStrategy extends AUniformDisplayStrategy
{
    /** log4j category */
    private static Logger log = Logger.getLogger(LinkDisplayStrategy.class);

    protected String getDisplayForValue(HttpServletRequest hrq, String value, int itemid)
    {
        return value;
    }

}
