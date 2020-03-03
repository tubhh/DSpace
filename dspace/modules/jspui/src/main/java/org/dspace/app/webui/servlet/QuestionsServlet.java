/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.itemexport.ItemExport;
import org.dspace.app.itemexport.ItemExportException;
import org.dspace.app.util.DCInputSet;
import org.dspace.app.util.DCInputsReader;
import org.dspace.app.webui.util.JSPManager;
import org.dspace.app.webui.util.UIUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.content.crosswalk.CrosswalkException;
import org.dspace.content.crosswalk.StreamDisseminationCrosswalk;
import org.dspace.content.integration.crosswalks.FileNameDisseminator;
import org.dspace.content.integration.crosswalks.StreamGenericDisseminationCrosswalk;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.core.LogManager;
import org.dspace.core.PluginManager;
import org.dspace.core.Utils;
import org.dspace.eperson.EPerson;
import org.dspace.utils.DSpace;

/**
 * Servlet for export in references format
 *
 * @author bollini
 *
 * @version $Revision: 1.1 $
 */
public class QuestionsServlet extends DSpaceServlet
{
    /** Logger */
    private static Logger log = Logger.getLogger(QuestionsServlet.class);


    DSpace dspace = new DSpace();

    @Override
    protected void doDSGet(Context context, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            SQLException, AuthorizeException
    {

        doDSPost(context, request, response);
    }

    protected void doDSPost(Context context, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            SQLException, AuthorizeException
    {
        int item_id = UIUtil.getIntParameter(request, "item_id");
        Item item = Item.find(context, item_id);

        String sTitle = "";
        String sDate = "";
        String sAuthor = "";
        String sLink = "";
        Metadatum[] mTitle = null;
        Metadatum[] mDate = null;
        Metadatum[] mLink = null;

        mTitle = item.getDC("title", null, Item.ANY);
        if (mTitle.length > 0)
        {
          sTitle = mTitle[0].value;
        }
        else
        {
          sTitle = "";
        }

        mDate = item.getDC("date", "issued", Item.ANY);
        if (mDate.length> 0)
        {
          sDate = mDate[0].value;
        }
        else
        {
          sDate = "";
        }

        for (Metadatum ma : item.getDC("contributor", "author", Item.ANY))
        {
          sAuthor = ("".equals(sAuthor) ? "" : sAuthor + " ; ") + ma.value;
        }

        mLink = item.getDC("identifier", "uri", Item.ANY);
        if (mLink.length > 0)
        {
          sLink = mLink[0].value;
        }
        else
        {
          sLink = "";
        }

        try
        {
            Email email = Email.getEmail(I18nUtil.getEmailFilename(context.getCurrentLocale(), "publication_question"));
            email.addRecipient(ConfigurationManager.getProperty("mail.publication-question.mailto"));
            // Eventually you need to change the order of the arguments when editing the template
            email.addArgument(sTitle); // Title
            email.addArgument(sDate); // issued
            email.addArgument(sAuthor); // Authors
            email.addArgument(sLink); // Link
            email.addArgument(request.getParameter("q")); // Question
            email.addArgument(request.getParameter("name")); // Name
            email.addArgument(request.getParameter("mail")); // E-MAil
            email.setReplyTo(request.getParameter("mail"));

            email.send();

            response.setStatus(204);
        }
        catch (Exception e)
        {
            log.warn(LogManager.getHeader(context, "emailError",
                    e.getMessage()), e);
            response.setStatus(500);
        }
    }
}