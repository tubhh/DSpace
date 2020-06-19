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

public class LicenseIconDisplayStrategy extends ASimpleDisplayStrategy
{
	/** log4j category */
    private static Logger log = Logger.getLogger(LicenseIconDisplayStrategy.class);
    
    public String getMetadataDisplay(HttpServletRequest hrq, int limit,
            boolean viewFull, String browseType, int colIdx, int itemid, String field,
            Metadatum[] metadataArray, boolean disableCrossLinks, boolean emph) throws JspException
    {
        String metadata = null;
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

        // License defaults to copyright
        metadata = "<a href='http://rightsstatements.org/vocab/InC/1.0/' target='_blank'><img src='/image/InC.Icon-Only.dark.png' alt='In Copyright' style='height:21px' />In Copyright</a>";

        if (metadataArray.length > 0)
        {
            String creativecommons = null;
            String creativecommonslink = null;
            for (Metadatum cc : metadataArray) {
                if (cc.value.length() >= 27 && (cc.value.substring(7,26).equals("creativecommons.org") || cc.value.substring(8,27).equals("creativecommons.org"))) {
                    String[] creativecommonsArray = cc.value.split("/");
                    if (creativecommonsArray[creativecommonsArray.length-1].equals("deed.de")) {
                        creativecommons = creativecommonsArray[creativecommonsArray.length-3]+"/"+creativecommonsArray[creativecommonsArray.length-2];
                    } else if (creativecommonsArray[creativecommonsArray.length-1].equals("de")) {
                        creativecommons = creativecommonsArray[creativecommonsArray.length-3]+"/"+creativecommonsArray[creativecommonsArray.length-2];
                    } else {
                        creativecommons = creativecommonsArray[creativecommonsArray.length-2]+"/"+creativecommonsArray[creativecommonsArray.length-1];
                    } 
                    creativecommonslink = cc.value;
                }
            }
            if (creativecommons != "") {
                if (creativecommonslink == "https://creativecommons.org/share-your-work/public-domain/cc0/") {
                    metadata = "<a href='https://creativecommons.org/share-your-work/public-domain/cc0/'><img src='http://i.creativecommons.org/p/zero/1.0/88x31.png' alt='CC Null' title='CC Null' /></a>";
                } else if (creativecommonslink == "https://creativecommons.org/share-your-work/public-domain/pdm/") {
                    metadata = "<a href='https://creativecommons.org/share-your-work/public-domain/pdm/'><img src='http://i.creativecommons.org/p/mark/1.0/88x31.png' alt='Public Domain' title='Public Domain' /></a>";
                } else {
                    metadata = "<a href='"+creativecommonslink+"'><img src='https://licensebuttons.net/l/"+creativecommons+"/88x31.png' alt='"+creativecommonslink+"' title='"+creativecommonslink+"' /></a>";
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
