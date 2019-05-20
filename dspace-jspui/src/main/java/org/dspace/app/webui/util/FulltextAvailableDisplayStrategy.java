/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.util;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.content.DCDate;
import org.dspace.core.Context;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.I18nUtil;

public class FulltextAvailableDisplayStrategy extends ASimpleDisplayStrategy
{
	/** log4j category */
    private static Logger log = Logger.getLogger(FulltextAvailableDisplayStrategy.class);
    
    public String getMetadataDisplay(HttpServletRequest hrq, int limit,
            boolean viewFull, String browseType, int colIdx, int itemid, String field,
            Metadatum[] metadataArray, boolean disableCrossLinks, boolean emph) throws JspException
    {
        String metadata = I18nUtil.getMessage("jsp.mydspace.render.nofulltext", hrq.getLocale());
/*
        if (metadataArray.length == 0) {
            try {
                // Virtual metadata is not in the metadataArray, so we have to obtain it newly
                Context obtainContext = UIUtil.obtainContext(hrq);
                Item item = Item.find(obtainContext, itemid);
                String[] parts = field.split("\\.");
                String schema = Item.ANY;
                String element = Item.ANY;
                String qualifier = Item.ANY;
                if (parts.length > 0) {
                    schema = parts[0];
                }
                if (parts.length > 1) {
                    element = parts[1];
                }
                if (parts.length > 2) {
                    qualifier = parts[2];
                }
                metadataArray = item.getMetadata(schema, element, qualifier, Item.ANY);
            } catch (SQLException e) {
                throw new JspException(e);
            }
        }
*/
        if (metadataArray.length > 0)
        {
            String value = metadataArray[0].value;
            if(!StringUtils.isBlank(value)) {
                if(value.equals("With Fulltext")) {
                    metadata = I18nUtil.getMessage("jsp.mydspace.render.fulltext", hrq.getLocale());
                }
                else if (value.equals("No Fulltext")) {
                    metadata = I18nUtil.getMessage("jsp.mydspace.render.nofulltext", hrq.getLocale());
                }
                else {
                    metadata = value;
                }
            }
        }
        return metadata;
    }

    public String getExtraCssDisplay(HttpServletRequest hrq, int limit,
            boolean b, String string, int colIdx, int itemid, String field,
            Metadatum[] metadataArray, boolean disableCrossLinks, boolean emph,
            PageContext pageContext)
    {
        return "nowrap=\"nowrap\" align=\"right\"";
    }

}
