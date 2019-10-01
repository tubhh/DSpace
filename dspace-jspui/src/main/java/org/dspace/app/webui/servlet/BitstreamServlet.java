/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.dspace.app.util.IViewer;
import org.dspace.app.webui.util.JSPManager;
import org.dspace.app.webui.util.UIUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.core.PluginManager;
import org.dspace.core.Utils;
import org.dspace.disseminate.CitationDocument;
import org.dspace.disseminate.CoverPageService;
import org.dspace.handle.HandleManager;
import org.dspace.plugin.BitstreamHomeProcessor;
import org.dspace.usage.UsageEvent;
import org.dspace.utils.DSpace;

/**
 * Servlet for retrieving bitstreams. The bits are simply piped to the user. If
 * there is an <code>If-Modified-Since</code> header, only a 304 status code
 * is returned if the containing item has not been modified since that date.
 * <P>
 * <code>/bitstream/handle/sequence_id/filename</code>
 * 
 * @author Robert Tansley
 * @version $Revision$
 */
public class BitstreamServlet extends DSpaceServlet
{
    /** log4j category */
    private static Logger log = Logger.getLogger(BitstreamServlet.class);

    /**
     * Threshold on Bitstream size before content-disposition will be set.
     */
    private int threshold;
    
    @Override
	public void init(ServletConfig arg0) throws ServletException {

		super.init(arg0);
		threshold = ConfigurationManager
				.getIntProperty("webui.content_disposition_threshold");
	}

    @Override
	protected void doDSGet(Context context, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            SQLException, AuthorizeException
    {
    	Item item = null;
    	Bitstream bitstream = null;

        // Get the ID from the URL
        String idString = request.getPathInfo();
        String handle = "";
        String sequenceText = "";
        String filename = null;
        int sequenceID;

        if (idString == null)
        {
            idString = "";
        }
        
        // Parse 'handle' and 'sequence' (bitstream seq. number) out
        // of remaining URL path, which is typically of the format:
        // {handle}/{sequence}/{bitstream-name}
        // But since the bitstream name MAY have any number of "/"s in
        // it, and the handle is guaranteed to have one slash, we
        // scan from the start to pick out handle and sequence:

        // Remove leading slash if any:
        if (idString.startsWith("/"))
        {
            idString = idString.substring(1);
        }

        // skip first slash within handle
        int slashIndex = idString.indexOf('/');
        if (slashIndex != -1)
        {
            slashIndex = idString.indexOf('/', slashIndex + 1);
            if (slashIndex != -1)
            {
                handle = idString.substring(0, slashIndex);
                int slash2 = idString.indexOf('/', slashIndex + 1);
                if (slash2 != -1)
                {
                    sequenceText = idString.substring(slashIndex+1,slash2);
                    filename = idString.substring(slash2+1);
                }
            }
        }

        try
        {
            sequenceID = Integer.parseInt(sequenceText);
        }
        catch (NumberFormatException nfe)
        {
            sequenceID = -1;
        }
        
        // Now try and retrieve the item
        DSpaceObject dso = HandleManager.resolveToObject(context, handle);
        
        // Make sure we have valid item and sequence number
        if (dso != null && dso.getType() == Constants.ITEM && sequenceID >= 0)
        {
            item = (Item) dso;
        
            if (item.isWithdrawn())
            {
                log.info(LogManager.getHeader(context, "view_bitstream",
                        "handle=" + handle + ",withdrawn=true"));
                JSPManager.showJSP(request, response, "/tombstone.jsp");
                return;
            }

            boolean found = false;

            Bundle[] bundles = item.getBundles();

            for (int i = 0; (i < bundles.length) && !found; i++)
            {
                Bitstream[] bitstreams = bundles[i].getBitstreams();

                for (int k = 0; (k < bitstreams.length) && !found; k++)
                {
                    if (sequenceID == bitstreams[k].getSequenceID())
                    {
                        bitstream = bitstreams[k];
                        found = true;
                    }
                }
            }
        }

        if (bitstream == null || filename == null
                || !filename.equals(bitstream.getName()))
        {
            // No bitstream found or filename was wrong -- ID invalid
            log.info(LogManager.getHeader(context, "invalid_id", "path="
                    + idString));
            JSPManager.showInvalidIDError(request, response, idString,
                    Constants.BITSTREAM);

            return;
        }

        log.info(LogManager.getHeader(context, "view_bitstream",
                "bitstream_id=" + bitstream.getID()));
 
		if (bitstream.getMetadataValue(IViewer.METADATA_STRING_PROVIDER).contains(IViewer.STOP_DOWNLOAD)
				&& !AuthorizeManager.isAdmin(context, bitstream)) {
			throw new AuthorizeException("Download not allowed by viewer policy");
		}
        //new UsageEvent().fire(request, context, AbstractUsageEvent.VIEW,
		//		Constants.BITSTREAM, bitstream.getID());

        new DSpace().getEventService().fireEvent(
        		new UsageEvent(
        				UsageEvent.Action.VIEW, 
        				request, 
        				context, 
        				bitstream));
        
        // Modification date
        // Only use last-modified if this is an anonymous access
        // - caching content that may be generated under authorisation
        //   is a security problem
        if (context.getCurrentUser() == null)
        {
            // TODO: Currently the date of the item, since we don't have dates
            // for files
            response.setDateHeader("Last-Modified", item.getLastModified()
                    .getTime());

            // Check for if-modified-since header
            long modSince = -1;
            try {
            	modSince = request.getDateHeader("If-Modified-Since");
            }
            catch (IllegalArgumentException ex) {
            	// ignore the exception, the header is invalid 
            	// we proceed as it was not supplied/supported
            	// we have some bad web client that provide unvalid values 
            	// no need to fill our log with such exceptions
            }

            if (modSince != -1 && item.getLastModified().getTime() < modSince)
            {
                // Item has not been modified since requested date,
                // hence bitstream has not; return 304
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        }
        
        preProcessBitstreamHome(context, request, response, bitstream);
        
    	InputStream is = null;
    	
    	CoverPageService coverService = new DSpace().getSingletonService(CoverPageService.class);
    	Collection owningColl = item.getOwningCollection();
    	String collHandle="";
    	if(owningColl != null) {
    		collHandle= owningColl.getHandle();
    	}
    	String configFile =coverService.getConfigFile(collHandle);
        if (StringUtils.isNotBlank(configFile)
                && coverService.isValidType(bitstream))
        {
            // Pipe the bits

            try
            {
                CitationDocument citationDocument = new CitationDocument(
                        configFile);
                File citedDoc = citationDocument.makeCitedDocument(context,
                        bitstream, configFile);
                is = new FileInputStream(citedDoc);
                response.setHeader("Content-Length",
                        String.valueOf(citedDoc.length()));
            }
            catch (AuthorizeException e)
            {
                log.error(e.getMessage(), e);
                throw e;
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }

        }
        
        if(is == null) {
        	 is = bitstream.retrieve();
             response.setHeader("Content-Length", String
                     .valueOf(bitstream.getSize()));
        }
        
		// Set the response MIME type
        response.setContentType(bitstream.getFormat().getMIMEType());


		if(threshold != -1 && bitstream.getSize() >= threshold)
		{
			UIUtil.setBitstreamDisposition(bitstream.getName(), request, response);
		}

        //DO NOT REMOVE IT - WE NEED TO FREE DB CONNECTION TO AVOID CONNECTION POOL EXHAUSTION FOR BIG FILES AND SLOW DOWNLOADS
        context.complete();

        Utils.bufferedCopy(is, response.getOutputStream());
        is.close();
        response.getOutputStream().flush();
    }
    
    private void preProcessBitstreamHome(Context context, HttpServletRequest request,
            HttpServletResponse response, Bitstream item)
        throws ServletException, IOException, SQLException
    {
        try
        {
            BitstreamHomeProcessor[] chp = (BitstreamHomeProcessor[]) PluginManager.getPluginSequence(BitstreamHomeProcessor.class);
            for (int i = 0; i < chp.length; i++)
            {
                chp[i].process(context, request, response, item);
            }
        }
        catch (Exception e)
        {
            log.error("caught exception: ", e);
            throw new ServletException(e);
        }
    }
}
