/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.util;

import java.text.DateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.content.DCDate;
import org.dspace.content.Metadatum;
import org.dspace.core.I18nUtil;

public class AccessPolicyDisplayStrategy extends ASimpleDisplayStrategy
{
	/** log4j category */
    private static Logger log = Logger.getLogger(AccessPolicyDisplayStrategy.class);
    
    public String getMetadataDisplay(HttpServletRequest hrq, int limit,
            boolean viewFull, String browseType, int colIdx, int itemid, String field,
            Metadatum[] metadataArray, boolean disableCrossLinks, boolean emph)
    {
        String metadata = null;
        if (metadataArray.length > 0)
        {
            String value = metadataArray[0].value;
            metadata = value;
            if(!StringUtils.isBlank(value) && value.length() > 7) {
                if(value.substring(0,7).equals("embargo")) {
                    DCDate dd = new DCDate(value.substring(8));
                    String interimdate = value.substring(8);
log.debug("Datestring: "+interimdate);
log.debug("Year: "+interimdate.substring(0,4));
log.debug("Month: "+interimdate.substring(4,6));
log.debug("Day: "+interimdate.substring(6,8));
                    int year = Integer.parseInt(interimdate.substring(0,4));
                    int month = Integer.parseInt(interimdate.substring(4,6));
                    int day = Integer.parseInt(interimdate.substring(6,8));
                    GregorianCalendar cal = new GregorianCalendar(year, month, day);
                    Date embargoDate = cal.getTime();
                    try {
//LocaleSupport.getLocalizedMessage(pageContext, "org.dspace.app.webui.jsptag.ItemTag.embargo", new Object[] {DateFormat.getDateInstance(DateFormat.LONG, locale).format(embargoDate)}));
//                        metadata = "Embargoed until "+UIUtil.displayDate(dd, false, true, hrq);
                          metadata = "Embargoed until "+DateFormat.getDateInstance(DateFormat.LONG, hrq.getLocale()).format(embargoDate);
                    }
                    catch (RuntimeException rte) {
                        log.error("Malformed value for the DateDiplayStrategy " + rte.getMessage() + " - " + value.substring(9));
                        metadata = value;
                    }
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
