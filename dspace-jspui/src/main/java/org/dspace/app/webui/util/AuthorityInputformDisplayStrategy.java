/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import java.sql.SQLException;
import javax.servlet.jsp.PageContext;

import org.apache.log4j.Logger;
import org.dspace.content.Metadatum;
import org.dspace.app.util.Util;

public class AuthorityInputformDisplayStrategy extends ASimpleDisplayStrategy
{
    /** log4j category */
    private static Logger log = Logger
            .getLogger(AuthorityDisplayStrategy.class);

    @Override
    public String getMetadataDisplay(HttpServletRequest hrq, int limit,
            boolean viewFull, String browseType, int colIdx, int itemid, String field,
            Metadatum[] metadataArray, boolean disableCrossLinks, boolean emph) throws JspException
    {
        String metadata;
        // limit the number of records if this is the author field (if
        // -1, then the limit is the full list)
        boolean truncated = false;
        int loopLimit = metadataArray.length;
        if (limit != -1)
        {
            loopLimit = (limit > metadataArray.length ? metadataArray.length
                    : limit);
            truncated = (limit < metadataArray.length);
            log.debug("Limiting output of field " + field + " to "
                    + Integer.toString(loopLimit) + " from an original "
                    + Integer.toString(metadataArray.length));
        }

        StringBuffer sb = new StringBuffer();
        String[] fieldArray = field.split(".");
        Locale loc = new Locale("de");
//        for (int j = 0; j < loopLimit; j++)
//        {
            //If the values are in controlled vocabulary and the display value should be shown
            List<String> displayValues = new ArrayList<String>();
            try {
                displayValues = Util.getControlledVocabulariesDisplayValueLocalized(null, metadataArray, fieldArray[0], fieldArray[1], fieldArray[2], loc);
                if (displayValues != null && !displayValues.isEmpty())
                {
                    for (int d = 0; d < displayValues.size(); d++)
                    {
                        sb.append(displayValues.get(d));
                        if (d<displayValues.size()-1)  sb.append(" <br/>");
                    }
                }
            }
            catch (Exception e) {
                log.error(e.getMessage());
            }
//        }
        metadata = sb.toString();
log.info("AuthorityInputformDisplayStrategy setting metadata to resolved value "+metadata);
        return metadata;
    }

}
