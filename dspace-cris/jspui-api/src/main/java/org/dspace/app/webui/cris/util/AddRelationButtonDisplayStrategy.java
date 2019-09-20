/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.cris.util;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.webui.util.IDisplayMetadataValueStrategy;
import org.dspace.browse.BrowseDSpaceObject;
import org.dspace.browse.BrowseItem;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.I18nUtil;
import org.dspace.discovery.IGlobalSearchResult;

public class AddRelationButtonDisplayStrategy implements IDisplayMetadataValueStrategy
{

    public static final Log log = LogFactory
            .getLog(AddRelationButtonDisplayStrategy.class);

    @Override
    public String getMetadataDisplay(HttpServletRequest hrq, int limit, boolean viewFull, String browseType, int colIdx,
            String field, Metadatum[] metadataArray, BrowseItem item, boolean disableCrossLinks, boolean emph)
            throws JspException {
        ACrisObject cris = (ACrisObject) ((BrowseDSpaceObject) item).getBrowsableDSpaceObject();
        if (hrq.getAttribute("added-crisIDs") != null
                && Arrays.asList(((String)hrq.getAttribute("added-crisIDs")).split(",")).contains(cris.getCrisID())) {
            return "<span>"
                    + I18nUtil.getMessage("jsp.layout.cris.addrelations.added")
                    + "</span>";
        }
        else {
            return "<a class=\"btn btn-default\" href=\""
                    + "addrelations?"
                    + hrq.getQueryString() + "&selected-crisID=" + cris.getCrisID() + "\""
                    + ">"
                    + I18nUtil.getMessage("jsp.layout.cris.addrelations.button")
                    + "</a>";
        }
    }

    @Override
    public String getMetadataDisplay(HttpServletRequest hrq, int limit, boolean viewFull, String browseType, int colIdx,
            String field, Metadatum[] metadataArray, Item item, boolean disableCrossLinks, boolean emph)
            throws JspException {
        if (hrq.getAttribute("added-itemIDs") != null
                && Arrays.asList(((String)hrq.getAttribute("added-itemIDs")).split(",")).contains(String.valueOf(item.getID()))) {
            return "<span>"
                    + I18nUtil.getMessage("jsp.layout.cris.addrelations.added")
                    + "</span>";
        }
        else {
            return "<a class=\"btn btn-default\" href=\""
                    + "addrelations?"
                    + hrq.getQueryString() + "&selected-itemID=" + item.getID() + "\""
                    + ">"
                    + I18nUtil.getMessage("jsp.layout.cris.addrelations.button") + "</a>";
        }
    }

    @Override
    public String getExtraCssDisplay(HttpServletRequest hrq, int limit, boolean b, String browseType, int colIdx,
            String field, Metadatum[] metadataArray, BrowseItem browseItem, boolean disableCrossLinks, boolean emph)
            throws JspException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getExtraCssDisplay(HttpServletRequest hrq, int limit, boolean b, String browseType, int colIdx,
            String field, Metadatum[] metadataArray, Item item, boolean disableCrossLinks, boolean emph)
            throws JspException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getMetadataDisplay(HttpServletRequest hrq, int limit, boolean viewFull, String browseType, int colIdx,
            String field, Metadatum[] metadataArray, IGlobalSearchResult item, boolean disableCrossLinks, boolean emph)
            throws JspException {
        // TODO Auto-generated method stub
        return null;
    }

}
