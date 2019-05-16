<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>
<%--
  - Renders a whole HTML page for displaying item metadata.  Simply includes
  - the relevant item display component in a standard HTML page.
  -
  - Attributes:
  -    display.all - Boolean - if true, display full metadata record
  -    item        - the Item to display
  -    collections - Array of Collections this item appears in.  This must be
  -                  passed in for two reasons: 1) item.getCollections() could
  -                  fail, and we're already committed to JSP display, and
  -                  2) the item might be in the process of being submitted and
  -                  a mapping between the item and collection might not
  -                  appear yet.  If this is omitted, the item display won't
  -                  display any collections.
  -    admin_button - Boolean, show admin 'edit' button
  -    submitter_button - Boolean show submitter 'new version' button
  --%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@ page contentType="text/html;charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>

<%@ page import="org.dspace.content.Collection" %>
<%@ page import="org.dspace.content.Metadatum" %>
<%@ page import="org.dspace.content.Item" %>
<%@ page import="org.dspace.core.ConfigurationManager" %>
<%@ page import="org.dspace.handle.HandleManager" %>
<%@ page import="org.dspace.license.CreativeCommons" %>
<%@page import="javax.servlet.jsp.jstl.fmt.LocaleSupport"%>
<%@page import="org.dspace.versioning.Version"%>
<%@page import="org.dspace.core.Context"%>
<%@page import="org.dspace.app.webui.util.VersionUtil"%>
<%@page import="org.dspace.app.webui.util.UIUtil"%>
<%@page import="org.dspace.authorize.AuthorizeManager"%>
<%@page import="java.util.List"%>
<%@page import="org.dspace.core.Constants"%>
<%@page import="org.dspace.eperson.EPerson"%>
<%@page import="org.dspace.versioning.VersionHistory"%>
<%@page import="org.dspace.app.webui.servlet.MyDSpaceServlet"%>
<%--<%@page import="eu.zbw.EconStor.BibTeXGenerator.Generator"%>--%>

<%
    // Attributes
    Boolean displayAllBoolean = (Boolean) request.getAttribute("display.all");
    boolean displayAll = (displayAllBoolean != null && displayAllBoolean.booleanValue());
    Boolean suggest = (Boolean)request.getAttribute("suggest.enable");
    boolean suggestLink = (suggest == null ? false : suggest.booleanValue());
    Item item = (Item) request.getAttribute("item");
    Collection[] collections = (Collection[]) request.getAttribute("collections");
    Boolean admin_b = (Boolean)request.getAttribute("admin_button");
    boolean admin_button = (admin_b == null ? false : admin_b.booleanValue());
    Boolean submitter_b = (Boolean) request.getAttribute("submitter_button");
    boolean submitter_button = (submitter_b == null ? false : submitter_b.booleanValue());
    
    // get the workspace id if one has been passed
    Integer workspace_id = (Integer) request.getAttribute("workspace_id");

    // get the handle if the item has one yet
    String handle = item.getHandle();
    // get the doi if the item has one
    String doi = (String) request.getAttribute("doi");
    // get the preferred identifier (as URL)
    String preferredIdentifier = (String) request.getAttribute("preferred_identifier");
    // get the latestVersionIdentifier
    String latestVersionIdentifier = (String)request.getAttribute("versioning.latest_version_identifier");

    // CC URL & RDF
    String cc_url = CreativeCommons.getLicenseURL(item);
    String cc_rdf = CreativeCommons.getLicenseRDF(item);
    String creativecommons = "";
    String creativecommonslink = "";

    // Full title needs to be put into a string to use as tag argument
    String title = "";
    if (handle == null)
 	{
		title = "Workspace Item";
	}
	else 
	{
		Metadatum[] titleValue = item.getDC("title", null, Item.ANY);
		if (titleValue.length != 0)
		{
			title = titleValue[0].value;
		}
		else
		{
			title = "Item " + handle;
		}
	}
    boolean pmcEnabled = ConfigurationManager.getBooleanProperty("cris","pmc.enabled",false);
    boolean scopusEnabled = ConfigurationManager.getBooleanProperty("cris","ametrics.elsevier.scopus.enabled",false);
    boolean wosEnabled = ConfigurationManager.getBooleanProperty("cris","ametrics.thomsonreuters.wos.enabled",false);
    String doiMd = item.getMetadata("dc.identifier.doi");
    boolean scholarEnabled = ConfigurationManager.getBooleanProperty("cris","ametrics.google.scholar.enabled",false);
    boolean altMetricEnabled = ConfigurationManager.getBooleanProperty("cris","ametrics.altmetric.enabled",false) && StringUtils.isNotBlank(doiMd);
    
    Boolean versioningEnabledBool = (Boolean)request.getAttribute("versioning.enabled");
    boolean versioningEnabled = (versioningEnabledBool!=null && versioningEnabledBool.booleanValue());
    Boolean hasVersionButtonBool = (Boolean)request.getAttribute("versioning.hasversionbutton");
    Boolean hasVersionHistoryBool = (Boolean)request.getAttribute("versioning.hasversionhistory");
    boolean hasVersionButton = (hasVersionButtonBool!=null && hasVersionButtonBool.booleanValue());
    boolean hasVersionHistory = (hasVersionHistoryBool!=null && hasVersionHistoryBool.booleanValue());
    
    Boolean newversionavailableBool = (Boolean)request.getAttribute("versioning.newversionavailable");
    boolean newVersionAvailable = (newversionavailableBool!=null && newversionavailableBool.booleanValue());
    Boolean showVersionWorkflowAvailableBool = (Boolean)request.getAttribute("versioning.showversionwfavailable");
    boolean showVersionWorkflowAvailable = (showVersionWorkflowAvailableBool!=null && showVersionWorkflowAvailableBool.booleanValue());
    
    String latestVersionHandle = (String)request.getAttribute("versioning.latestversionhandle");
    String latestVersionURL = (String)request.getAttribute("versioning.latestversionurl");
    
    VersionHistory history = (VersionHistory)request.getAttribute("versioning.history");
    List<Version> historyVersions = (List<Version>)request.getAttribute("versioning.historyversions");
    
    EPerson user = (EPerson) request.getAttribute("dspace.current.user");
    boolean dedupEnabled = ConfigurationManager.getBooleanProperty("deduplication", "deduplication.admin.feature", false);
    
	boolean exportBiblioEnabled =  ConfigurationManager.getBooleanProperty("exportcitation.item.enabled", false);
	boolean exportBiblioAll =  ConfigurationManager.getBooleanProperty("exportcitation.show.all", false);
	String cfg = ConfigurationManager.getProperty("exportcitation.options");
	boolean coreRecommender = ConfigurationManager.getBooleanProperty("core-aggregator","core-aggregator.enabled");
	String coreCredentials = ConfigurationManager.getProperty("core-aggregator", "core-aggregator.credentials");
	
	String crisID = (String)request.getAttribute("crisID");
%>

<% if(pmcEnabled || scopusEnabled || wosEnabled || scholarEnabled || altMetricEnabled) { %>
<c:set var="dspace.layout.head.last" scope="request">
<% if(altMetricEnabled) { %> 
<script type='text/javascript' src='https://d1bxh8uas1mnw7.cloudfront.net/assets/embed.js'></script>
<% } %>
<script type="text/javascript"><!--
j(document).ready(function() {

	<% if(altMetricEnabled) { %>
	j(function () {
	    j('div.altmetric-embed').on('altmetric:hide ', function () {
	    	j('div.altmetric').hide();
	    });
	});
	<% } %>
	
	<% if (dedupEnabled && admin_button) { %>
	j.ajax({
		url : "<%=request.getContextPath()%>/json/duplicate",
		data : {																			
			"itemid" : <%= item.getID()%>,
			"typeid" : "2",
			"admin": true
		},
		success : function(data) {
			if(data.iTotalDisplayRecords==0) {
				j('div.dedup').hide();
			}
			else {
				j('#dedupCounter').html(data.iTotalDisplayRecords);
				var queryString = "?";
				var tmp_itemid_list = <%= item.getID()%> + ",";
				j.each(data.aaData, function( index, value ) {
					tmp_itemid_list += value.entityID;
					tmp_itemid_list += ",";
				});				
				var itemid_list = tmp_itemid_list.substr(0, tmp_itemid_list.length-1);
				queryString += 'scope=0&submitcheck=submitcheck&itemid_list='+itemid_list;
				j('#dedupCounter').attr('href', '<%=request.getContextPath()%>/tools/duplicate' + queryString);
			}			
		},
		error : function(data) {
		}
	});
	<% } %>
});
--></script>
	<% if(coreRecommender) { %>
	<link rel="stylesheet" href="<%= request.getContextPath() %>/static/css/recommender/core.css" type="text/css" />	
	<script>
		(function(d, s, idScript, idRec, userInput) {
			var coreAddress = 'https://core.ac.uk/';
			var js, fjs = d.getElementsByTagName(s)[0];
			if (d.getElementById(idScript))
				return;
			js = d.createElement(s);
			js.id = idScript;
			js.src = coreAddress + 'recommender/embed.js';
			fjs.parentNode.insertBefore(js, fjs);
	
			localStorage.setItem('idRecommender', idRec);
			localStorage.setItem('userInput', JSON.stringify(userInput));
	
/* 			var link = d.createElement('link');
			link.setAttribute('rel', 'stylesheet');
			link.setAttribute('type', 'text/css');
			link.setAttribute('href', coreAddress
					+ 'recommender/embed-default-style.css');
			d.getElementsByTagName('head')[0].appendChild(link); */
		}(document, 'script', 'recommender-embed', '<%= coreCredentials %>', {}));
	</script>
	<% } %>
</c:set>
<% } %>

<dspace:layout title="<%= title %>">
<%
    if (handle != null)
    {
%>
	<div class="row">
		<div class="col-sm-<%= admin_button?"7":"12" %> col-md-<%= admin_button?"8":"12" %> col-lg-<%= admin_button?"9":"12" %>">
		<%		
		if (newVersionAvailable)
		   {
		%>
		<div class="alert alert-warning"><b><fmt:message key="jsp.version.notice.new_version_head"/></b>		
		<fmt:message key="jsp.version.notice.new_version_help"/><a href="<%=latestVersionIdentifier %>"><%= latestVersionIdentifier %></a>
		</div>
		<%
		    }
		%>
		
		<%		
		if (showVersionWorkflowAvailable)
		   {
		%>
		<div class="alert alert-warning"><b><fmt:message key="jsp.version.notice.workflow_version_head"/></b>		
		<fmt:message key="jsp.version.notice.workflow_version_help"/>
		</div>
		<%
		    }
		%>
		

                <%-- <strong>Please use this identifier to cite or link to this item:
                <code><%= HandleManager.getCanonicalForm(handle) %></code></strong>--%>
                <%-- <div class="well"><fmt:message key="jsp.display-item.identifier"/>
                <code><%= HandleManager.getCanonicalForm(handle) %></code></div>--%>
                <%
                    String urn = "";
                    if (StringUtils.isNotBlank(doiMd)) {
                %>
                    <div class="well"><fmt:message key="jsp.display-item.identifier"/>
                    <a href="https://doi.org/<%= doiMd %>">https://doi.org/<%= doiMd %></a></div>
                <%
                    }
/*
                    else {
                    Metadatum[] dcv = item.getMetadata("dc", "identifier", "urn", Item.ANY);
                    Metadatum[] localdcv = item.getMetadata("tuhh", "identifier", "urn", Item.ANY);
                    if (localdcv.length != 0) {
                        urn = localdcv[0].value;
                    }
                    if (dcv.length != 0) {
                        urn = dcv[0].value;
                    }
                    if (urn != "") {
                %>
                    <div class="well"><fmt:message key="jsp.display-item.identifier"/>
                    <code>urn:<%= urn %></code> (<a href="http://nbn-resolving.de/?urn=<%= urn %>">Link</a>)</div>
                <%
                    }
                    }

                    if (StringUtils.isBlank(doiMd) && urn == "") {
                %>
                    <div class="well"><fmt:message key="jsp.display-item.identifier"/>
                    <code><%= HandleManager.getCanonicalForm(handle) %></code></div>
                <%
                    }
*/
                %>
    <%
        Metadatum[] hasFulltext = item.getMetadata("item", "fulltext", Item.ANY, Item.ANY);
        Metadatum[] isOA = item.getMetadata("openaire", "rights", Item.ANY, Item.ANY);
        Metadatum[] cccheck = item.getMetadata("dc", "rights", Item.ANY, Item.ANY);
        for (Metadatum cc : cccheck) {
            if (cc.value.length() >= 27 && (cc.value.substring(7,26).equals("creativecommons.org") || cc.value.substring(8,27).equals("creativecommons.org"))) {
                String[] creativecommonsArray = cc.value.split("/");
                if (creativecommonsArray[creativecommonsArray.length-1].equals("deed.de")) {
                    creativecommons = creativecommonsArray[creativecommonsArray.length-4]+"/"+creativecommonsArray[creativecommonsArray.length-3]+"/"+creativecommonsArray[creativecommonsArray.length-2];
                } else if (creativecommonsArray[creativecommonsArray.length-1].equals("de")) {
                    creativecommons = creativecommonsArray[creativecommonsArray.length-3]+"/"+creativecommonsArray[creativecommonsArray.length-2];
                } else {
                    creativecommons = creativecommonsArray[creativecommonsArray.length-2]+"/"+creativecommonsArray[creativecommonsArray.length-1];
                }
                creativecommonslink = cc.value;
            }
        }
        if (creativecommons != "") {
%>
                    <div class="well"><%-- <fmt:message key="jsp.display-item.creativecommons"/> --%>
                        <%
                            if (hasFulltext.length > 0 && hasFulltext[0].value.equals("With Fulltext")) {
                        %>
                                <fmt:message key="jsp.mydspace.render.fulltext" /><span style="padding-left:10px;">
                        <%
                            }
                        %>
                        <%
                            if (isOA.length > 0 && isOA[0].value.equals("info:eu-repo/semantics/openAccess")) {
                        %>
                                <fmt:message key="jsp.mydspace.render.oa" /><span style="padding-left:10px;">
                        <%
                            }
                        %>
                        <% if (creativecommonslink == "https://creativecommons.org/share-your-work/public-domain/cc0/") { %>
                            <a href="https://creativecommons.org/share-your-work/public-domain/cc0/">
                                <img src="http://i.creativecommons.org/p/zero/1.0/88x31.png" alt="CC Null" title="CC Null" />
                            </a>
                        <% } else if (creativecommonslink == "https://creativecommons.org/share-your-work/public-domain/pdm/") { %>
                            <a href="https://creativecommons.org/share-your-work/public-domain/pdm/">
                                <img src="http://i.creativecommons.org/p/mark/1.0/88x31.png" alt="Public Domain" title="Public Domain" />
                            </a>
                        <% } else { %>
                            <a href="<%= creativecommonslink %>">
                                <img src="https://licensebuttons.net/l/<%= creativecommons %>/88x31.png" alt="<%= creativecommonslink %>" title="<%= creativecommonslink %>" />
                            </a>
                        <% } %>
                    </div>
    <%
        } else {

        Metadatum[] cc = item.getMetadata("dc", "rights", "cc", Item.ANY);
        Metadatum[] ccv = item.getMetadata("dc", "rights", "ccversion", Item.ANY);
        if (cc.length != 0) {
            creativecommonslink = creativecommons = cc[0].value;
        }
        if (ccv.length != 0) {
            creativecommonslink += "/" + ccv[0].value;
        }
        if (creativecommons != "") {
    %>
                    <div class="well"><%-- <fmt:message key="jsp.display-item.creativecommons"/> --%>
                        <%
                            if (hasFulltext.length > 0 && hasFulltext[0].value.equals("With Fulltext")) {
                        %>
                                <fmt:message key="jsp.mydspace.render.fulltext" /><span style="padding-left:10px;"></span>
                        <%
                            }
                        %>
                        <%
                            if (isOA.length > 0 && isOA[0].value.equals("info:eu-repo/semantics/openAccess")) {
                        %>
                                <fmt:message key="jsp.mydspace.render.oa" /><span style="padding-left:10px;"></span>
                        <%
                            }
                        %>
                        <% if (creativecommons == "cc-null") { %>
                            <a href="https://creativecommons.org/share-your-work/public-domain/cc0/">
                                <img src="http://i.creativecommons.org/p/zero/1.0/88x31.png" alt="CC Null" title="CC Null" />
                            </a>
                        <% } else if (creativecommons == "pd") { %>
                            <a href="https://creativecommons.org/share-your-work/public-domain/pdm/">
                                <img src="http://i.creativecommons.org/p/mark/1.0/88x31.png" alt="Public Domain" title="Public Domain" />
                            </a>
                        <% } else { %>
                            <a href="https://creativecommons.org/licenses/<%= creativecommonslink %>">
                                <img src="https://licensebuttons.net/l/<%= creativecommonslink %>/88x31.png" alt="<%= creativecommonslink %>" title="<%= creativecommonslink %> "/>
                            </a>
                        <% } %>
                    </div>
    <%
        } else if ((hasFulltext.length > 0 && hasFulltext[0].value.equals("With Fulltext")) || (isOA.length > 0 && isOA[0].value.equals("info:eu-repo/semantics/openAccess"))) {
    %>
                    <div class="well">
                        <%
                            if (hasFulltext.length > 0 && hasFulltext[0].value.equals("With Fulltext")) {
                        %>
                                <fmt:message key="jsp.mydspace.render.fulltext" /><span style="padding-left:10px;">
                        <%
                            }
                        %>
                        <%
                            if (isOA.length > 0 && isOA[0].value.equals("info:eu-repo/semantics/openAccess")) {
                        %>
                                <fmt:message key="jsp.mydspace.render.oa" />
                        <%
                            }
                        %>
                    </div>
    <%
        }
        }
    %>

       </div>         
<%
        if (admin_button)  // admin edit button
        {
%>
        <div class="col-sm-5 col-md-4 col-lg-3">
            <div class="panel panel-warning">
            	<div class="panel-heading"><fmt:message key="jsp.admintools"/></div>
            	<div class="panel-body">
				<form method="get" action="<%= request.getContextPath() %>/submit">
                    <input type="hidden" name="edit_item" value="<%= item.getID() %>" />
                    <input type="hidden" name="pageCallerID" value="0" />
                    <%--<input type="submit" name="submit" value="Edit...">--%>
                    <input class="btn btn-default col-md-12" type="submit" name="submit" value="<fmt:message key="jsp.general.editsubmission.button"/>" />
                </form>
                <form method="get" action="<%= request.getContextPath() %>/tools/edit-item">
                    <input type="hidden" name="item_id" value="<%= item.getID() %>" />
                    <%--<input type="submit" name="submit" value="Edit...">--%>
                    <input class="btn btn-default col-md-12" type="submit" name="submit" value="<fmt:message key="jsp.general.editnormal.button"/>" />
                </form>
                <form method="post" action="<%= request.getContextPath() %>/mydspace">
                    <input type="hidden" name="item_id" value="<%= item.getID() %>" />
                    <input type="hidden" name="step" value="<%= MyDSpaceServlet.REQUEST_EXPORT_ARCHIVE %>" />
                    <input class="btn btn-default col-md-12" type="submit" name="submit" value="<fmt:message key="jsp.mydspace.request.export.item"/>" />
                </form>
                <form method="post" action="<%= request.getContextPath() %>/mydspace">
                    <input type="hidden" name="item_id" value="<%= item.getID() %>" />
                    <input type="hidden" name="step" value="<%= MyDSpaceServlet.REQUEST_MIGRATE_ARCHIVE %>" />
                    <input class="btn btn-default col-md-12" type="submit" name="submit" value="<fmt:message key="jsp.mydspace.request.export.migrateitem"/>" />
                </form>
                <form method="post" action="<%= request.getContextPath() %>/dspace-admin/metadataexport">
                    <input type="hidden" name="handle" value="<%= item.getHandle() %>" />
                    <input class="btn btn-default col-md-12" type="submit" name="submit" value="<fmt:message key="jsp.general.metadataexport.button"/>" />
                </form>
					<% if(hasVersionButton) { %>       
                	<form method="get" action="<%= request.getContextPath() %>/tools/version">
                    	<input type="hidden" name="itemID" value="<%= item.getID() %>" />                    
                    	<input class="btn btn-default col-md-12" type="submit" name="submit" value="<fmt:message key="jsp.general.version.button"/>" />
                	</form>
                	<% } %> 
                	<% if(hasVersionHistory) { %>			                
                	<form method="get" action="<%= request.getContextPath() %>/tools/history">
                    	<input type="hidden" name="itemID" value="<%= item.getID() %>" />
                    	<input type="hidden" name="versionID" value="<%= history.getVersion(item)!=null?history.getVersion(item).getVersionId():null %>" />                    
                    	<input class="btn btn-info col-md-12" type="submit" name="submit" value="<fmt:message key="jsp.general.version.history.button"/>" />
                	</form>         	         	
					<% } %>
             </div>
          </div>
        </div>
<%      } %>

</div>
<%
    }

    String displayStyle = (displayAll ? "full" : "");
%>

<div class="row">
<div id="wrapperDisplayItem" class="col-lg-9">
    <dspace:item-preview item="<%= item %>" />
    <dspace:item item="<%= item %>" collections="<%= collections %>" style="<%= displayStyle %>" />
    <%-- SFX Link --%>
<%
    if (ConfigurationManager.getProperty("sfx.server.url") != null)
    {
        String sfximage = ConfigurationManager.getProperty("sfx.server.image_url");
        if (sfximage == null)
        {
            sfximage = request.getContextPath() + "/image/sfx-link.gif";
        }
%>
        <a class="btn btn-default" href="<dspace:sfxlink item="<%= item %>"/>" /><img src="<%= sfximage %>" border="0" alt="SFX Query" /></a>
<%
    }
%>

<%
    String locationLink = request.getContextPath() + "/handle/" + handle;

    if (displayAll)
    {
%>
<%
        if (workspace_id != null)
        {
%>
    <form class="pull-left" method="post" action="<%= request.getContextPath() %>/view-workspaceitem">
        <input type="hidden" name="workspace_id" value="<%= workspace_id.intValue() %>" />
        <input class="btn btn-default" type="submit" name="submit_simple" value="<fmt:message key="jsp.display-item.text1"/>" />
    </form>
<%
        }
        else
        {
%>
    <a class="btn btn-default" href="<%=locationLink %>?mode=simple">
        <fmt:message key="jsp.display-item.text1"/>
    </a>
<%
        }
    }
    else
    {
        if (workspace_id != null)
        {
%>
    <form class="pull-left" method="post" action="<%= request.getContextPath() %>/view-workspaceitem">
        <input type="hidden" name="workspace_id" value="<%= workspace_id.intValue() %>" />
        <input class="btn btn-default" type="submit" name="submit_full" value="<fmt:message key="jsp.display-item.text2"/>" />
    </form>
<%
        }
        else
        {
%>
    <a class="btn btn-default" href="<%=locationLink %>?mode=full">
        <fmt:message key="jsp.display-item.text2"/>
    </a>
<%
        }
    }

    if (workspace_id != null)
    {
%>
   <form class="pull-left" method="post" action="<%= request.getContextPath() %>/workspace">
        <input type="hidden" name="workspace_id" value="<%= workspace_id.intValue() %>"/>
        <input class="btn btn-primary" type="submit" name="submit_open" value="<fmt:message key="jsp.display-item.back_to_workspace"/>"/>
    </form>
<%
    } else {
        if (suggestLink)
        {
%>
    <a class="btn btn-success" href="<%= request.getContextPath() %>/suggest?handle=<%= handle %>" target="_blank">
       <fmt:message key="jsp.display-item.suggest"/>
    </a>
<%
        }
%>
	<% if(coreRecommender) { %>	
	<br/>
	<br/>
	<div id="recommender" class="panel panel-default">
	<div class="panel-heading"><fmt:message key="jsp.display-item.recommender" /></div>
	
	<div class="panel-body">
	<div id="coreRecommenderOutput"></div>
	</div>
	</div>
	<% } %>
</div>
<div class="col-lg-3">
<div class="row">
<%
if (dedupEnabled && admin_button) { %>	
<div class="col-lg-12 col-md-4 col-sm-6">
<div class="media dedup">
	<div class="media-left">
		<fmt:message key="jsp.display-item.dedup.title"/>
	</div>
	<div id="dedupResult" class="media-body text-center">
		<h4 class="media-heading"><fmt:message key="jsp.display-item.dedup.heading"/></h4>
	    <span class="metric-counter"><a id="dedupCounter" data-toggle="tooltip" target="_blank" title="<fmt:message key="jsp.display-item.dedup.tooltip"/>" href=""><fmt:message key="jsp.display-item.dedup.check"/></a></span>
	</div>
</div>	
</div>
<br class="visible-lg" />
<% } %>
<c:forEach var="metricType" items="${metricTypes}">
<c:set var="metricNameKey">
	jsp.display-item.citation.${metricType}
</c:set>
<c:set var="metricIconKey">
	jsp.display-item.citation.${metricType}.icon
</c:set>
<c:if test="${not empty metrics[metricType].counter and metrics[metricType].counter gt 0}">
	<c:if test="${!empty metrics[metricType].moreLink}">
		<script type="text/javascript">
		j(document).ready(function() {
			var obj = JSON.parse('${metrics[metricType].moreLink}');
			j( "div" ).data( "moreLink", obj );
			j( "#metric-counter-${metricType}" ).wrap(function() {
				  return "<a target='_blank' href='" + j( "div" ).data( "moreLink" ).link + "'></a>";
			}).append(" <i class='fa fa-info-circle' data-toggle='tooltip' title='Get updated citations from database'></i>");
			
			jQuery('[data-toggle="tooltip"]').tooltip();
		});
		</script>
	</c:if>
<div class="col-lg-12 col-md-4 col-sm-6 col-xs-12 box-${metricType}">
<div class="media ${metricType}">
	<div class="media-left">
		<fmt:message key="${metricIconKey}"/>
	</div>
	<div class="media-body text-center">
		<h4 class="media-heading"><fmt:message key="${metricNameKey}"/>
		<c:if test="${not empty metrics[metricType].rankingLev}">
		<span class="pull-right">
		<fmt:message key="jsp.display-item.citation.top"/>		
		<span class="metric-ranking arc">
			<span class="circle" data-toggle="tooltip" data-placement="bottom" 
				title="<fmt:message key="jsp.display-item.citation.${metricType}.ranking.tooltip"><fmt:param><fmt:formatNumber value="${metrics[metricType].rankingLev}" type="NUMBER" maxFractionDigits="0" /></fmt:param></fmt:message>">
				<fmt:formatNumber value="${metrics[metricType].rankingLev}" 
					type="NUMBER" maxFractionDigits="0" />
			</span>
		</span>
		</span>
		</c:if>
		</h4>
		<span id="metric-counter-${metricType}" class="metric-counter">
		<c:choose>		
		<c:when test="${!empty metrics[metricType].formatter.type}">
		<c:choose>
		<c:when test="${!(metrics[metricType].formatter.type eq 'PERCENT')}">
			<fmt:formatNumber value="${metrics[metricType].counter}" 
				type="${metrics[metricType].formatter.type}"
				maxFractionDigits="${metrics[metricType].formatter.maxFractionDigits}"
				minFractionDigits="${metrics[metricType].formatter.minFractionDigits}"/>
		</c:when>
		<c:otherwise>
			<fmt:formatNumber value="${metrics[metricType].counter/100}" 
				type="${metrics[metricType].formatter.type}"
				maxFractionDigits="${metrics[metricType].formatter.maxFractionDigits}"
				minFractionDigits="${metrics[metricType].formatter.minFractionDigits}"/>
		</c:otherwise>
		</c:choose>
		</c:when>		
		<c:otherwise>
			<fmt:formatNumber value="${metrics[metricType].counter}" 
				pattern="${metrics[metricType].formatter.pattern}"/>
		</c:otherwise>
		</c:choose>
		</span>
	</div>
	<c:if test="${not empty metrics[metricType].last1}">
	<div class="row">
		<div class="col-xs-6 text-left">
			<fmt:message key="jsp.display-item.citation.last1" />
			<br/>
				<c:choose>		
					<c:when test="${!empty metrics[metricType].formatter.type}">
					<c:choose>
						<c:when test="${!(metrics[metricType].formatter.type eq 'PERCENT')}">
							<fmt:formatNumber value="${metrics[metricType].last1}" 
								type="${metrics[metricType].formatter.type}"
								maxFractionDigits="${metrics[metricType].formatter.maxFractionDigits}"
								minFractionDigits="${metrics[metricType].formatter.minFractionDigits}"/>
						</c:when>
						<c:otherwise>
							<fmt:formatNumber value="${metrics[metricType].last1/100}" 
								type="${metrics[metricType].formatter.type}"
								maxFractionDigits="${metrics[metricType].formatter.maxFractionDigits}"
								minFractionDigits="${metrics[metricType].formatter.minFractionDigits}"/>
						</c:otherwise>
					</c:choose>
					</c:when>		
					<c:otherwise>
						<fmt:formatNumber value="${metrics[metricType].last1}" 
							pattern="${metrics[metricType].formatter.pattern}"/>
					</c:otherwise>
				</c:choose>			
		</div>
		<div class="col-xs-6 text-right">
			<fmt:message key="jsp.display-item.citation.last2" />
			<br/>
				<c:choose>		
					<c:when test="${!empty metrics[metricType].formatter.type}">
					<c:choose>
						<c:when test="${!(metrics[metricType].formatter.type eq 'PERCENT')}">
							<fmt:formatNumber value="${metrics[metricType].last2}" 
								type="${metrics[metricType].formatter.type}"
								maxFractionDigits="${metrics[metricType].formatter.maxFractionDigits}"
								minFractionDigits="${metrics[metricType].formatter.minFractionDigits}"/>
						</c:when>
						<c:otherwise>
							<fmt:formatNumber value="${metrics[metricType].last2/100}" 
								type="${metrics[metricType].formatter.type}"
								maxFractionDigits="${metrics[metricType].formatter.maxFractionDigits}"
								minFractionDigits="${metrics[metricType].formatter.minFractionDigits}"/>
						</c:otherwise>
					</c:choose>
					</c:when>		
					<c:otherwise>
						<fmt:formatNumber value="${metrics[metricType].last2}" 
							pattern="${metrics[metricType].formatter.pattern}"/>
					</c:otherwise>
				</c:choose>				
		</div>
	</div>
	</c:if>
	<div class="row">
		<div class="col-lg-12 text-center small">
			<fmt:message
				key="jsp.display-item.citation.time">
				<fmt:param value="${metrics[metricType].time}" />
			</fmt:message>
		</div>
	</div>
	</div>
</div>
</c:if>
</c:forEach>
<!------------------------ OA-Statistik Box -----------------------------
<div class="col-lg-12 col-md-4 col-sm-6">
    <div class="media oas">
        <a class="statisticsLink" href="http://doku.b.tu-harburg.de/graphprovider/show_oas.php?identifier=oai:tubdok.tub.tuhh.de:<%= handle %>"><img src="<%= request.getContextPath() %>/image/logo_oas_gross.gif" width="200px" alt="OAS" /></a>
    </div>
</div>
------------------ Ende OA-Statistik Box ------------------------------------>
    <%
	   if(scholarEnabled) { %>
<div class="col-lg-12 col-md-4 col-sm-6">
<div class="media google">
	<div class="media-left">
		<fmt:message key="jsp.display-item.citation.google.icon">
			<fmt:param value="<%=request.getContextPath()%>" />
		</fmt:message>
	</div>
	<div id="googleCitedResult" class="media-body text-center">
		<h4 class="media-heading"><fmt:message key="jsp.display-item.citation.google"/></h4>
		
		
   		    <span class="metric-counter"><a data-toggle="tooltip" target="_blank" title="<fmt:message key="jsp.display-item.citation.google.tooltip"/>" href="https://scholar.google.com/scholar?as_q=&as_epq=<%= title %>&as_occt=any"><fmt:message key="jsp.display-item.citation.google.check"/></a></span>
	</div>
</div>	
</div>
<br class="visible-lg" />
    <% }
	   if(altMetricEnabled) { %>
<div class="col-lg-12 col-md-4 col-sm-6">
<div class="media altmetric">
	<div class="media-left">
      		<div class='altmetric-embed' data-hide-no-mentions="true" data-badge-details="right" data-condensed="true" data-badge-type="donut" data-link-target='_blank' data-doi="<%= doiMd %>" data-handle="<%= handle %>" data-uri="<%= HandleManager.getCanonicalForm(handle) %>"></div>
	</div>
<!--
	<div class="media-body media-middle text-center">
		<h4 class="media-heading"><fmt:message key="jsp.display-item.citation.altmetric"/></h4>
	</div>
-->
</div>
</div>
<% } %>
    </div>
</div>
<% if(pmcEnabled) { %>
<div class="modal fade" id="dialogPMC" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">

    </div>
  </div>
</div>
<% }%>
<%
    }
%>

<%--------- Feedback Box from Bamberg University ---------
      <div class="col-lg-12 col-md-4 col-sm-6">
        <div class="panel panel-info">
          <div class="panel-heading">
            <h3 class="panel-title larger-panel-title">
              <fmt:message key="jsp.display-item.info.links" />
            </h3>
          </div>
          <div class="panel-list">
            <%@ include file="layout/fragenmodal.html" %>
          </div>
        </div>
      </div>
------ End Feedback Box from Bamberg University -------------%>


<%-- As there is now only one possible User Tools button (create new version) it can be included
    in this check so an empty sub-menu will not be displayed --%>
<%-- if((submitter_button && hasVersionButton) || StringUtils.isNotBlank(crisID)) { --%>
<% if((submitter_button && hasVersionButton)) { %>
       <div class="col-sm-5 col-md-4 col-lg-3">
            <div class="panel panel-warning">
            	<div class="panel-heading"><fmt:message key="jsp.usertools"/></div>

<%--
            <% if(StringUtils.isNotBlank(crisID)) { %>
            	<div class="panel-body">
        			<a class="btn btn-primary col-md-12" href="<%= request.getContextPath() %>/tools/claim?handle=<%= handle %>">
            			<fmt:message key="jsp.display-item.claim-publication"/>
        			</a>    	
            	</div>
    <% } %>
--%>
<%
        // Include the hasVersionButton test here as well, in case the user tools menu gains additional buttons
        if (submitter_button && hasVersionButton) {
%>
                <div class="panel-body">
                    <form method="get" action="<%= request.getContextPath()%>/tools/version">
                        <input type="hidden" name="itemID" value="<%= item.getID()%>" />
                        <input class="btn btn-primary col-md-12" type="submit" name="submit" value="<fmt:message key="jsp.general.version.button"/>" />
                    </form>
                </div>
<%
        }
%>
            </div>
            </div>
<%
        }
%>
       <div class="col-sm-5 col-md-4 col-lg-3">
            <div class="panel panel-warning">
            	<div class="panel-heading"><fmt:message key="jsp.exporttools"/></div>
<%--
<!----------------- BibTeX-Export uebernommen von ZBW ------------------------->
<!--
    <div class="pannel-body">
        <a class="bibtexLink btn btn-primary" href="<%= request.getContextPath() %>/bibtexexport/<%= handle %>/<%= Generator.getBibTeXIdentifier(item) %>.bib">
            BibTeX-Export
        </a>
    </div>
-->
<!----------------- Ende BibTeX-Export ------------------------->
--%>
<!----------------- BibTeX-Export DSpace CRIS ------------------------->
    <%
    	if (exportBiblioEnabled && ( exportBiblioAll || user!=null ) ) {
    %>

    		<form target="blank" class="form-inline"  id="exportform" action="<%= request.getContextPath() %>/references">

    		<div id="export-biblio-panel">
    	<%		
    		if (cfg == null)
    		{
    			cfg = "refman, endnote, bibtex, refworks";
    		}
    		String[] cfgSplit = cfg.split("\\s*,\\s*");
    		for (String format : cfgSplit) {
    	%>
    		<c:set var="format"><%= format %></c:set>	    
    		<label class="radio-inline">
        		  <input id="${format}" type="radio" name="format" value="${format}" <c:if test="${format=='bibtex'}"> checked="checked"</c:if>/><fmt:message key="exportcitation.option.${format}" />
    	    </label>

    		
    	<% } %>
<%--
    		<label class="checkbox-inline">
    			<input type="checkbox" id="email" name="email" value="true"/><fmt:message key="exportcitation.option.email" />
    		</label>
--%>
    			<input type="hidden" name="item_id" value="<%= item.getID() %>" />
    			<input id="export-submit-button" class="bibtexLink btn btn-primary" type="submit" name="submit_export" value="<fmt:message key="exportcitation.option.submitexport" />" />
    		</div>	
    		</form>
    <% } %>
</div>
</div>
<!----------------- Ende BibTeX-Export ------------------------->
</div>
</div>
<div class="container">
    <%-- Versioning table --%>
<%
    if (versioningEnabled && hasVersionHistory && historyVersions.size() > 1)
    {
        boolean item_history_view_admin = ConfigurationManager
                .getBooleanProperty("versioning", "item.history.view.admin");
        boolean item_history_include_submitter = ConfigurationManager
                .getBooleanProperty("versioning", "item.history.include.submitter", false);
        if(!item_history_view_admin || admin_button) {
%>
	<div id="versionHistory" class="panel panel-info">
	<div class="panel-heading"><fmt:message key="jsp.version.history.head2" /></div>
	
	<table class="table panel-body">
		<tr>
			<th id="tt1" class="oddRowEvenCol"><fmt:message key="jsp.version.history.column1"/></th>
			<th 			
				id="tt2" class="oddRowOddCol"><fmt:message key="jsp.version.history.column2"/></th>
            <%-- Add Information about submitter only for admins or if item.history.include.submitter is true --%>
            <% if (item_history_include_submitter || admin_button) { %>
                <th id="tt3" class="oddRowEvenCol"><fmt:message key="jsp.version.history.column3"/></th>
            <% } %>
			<th 
				id="tt4" class="oddRowOddCol"><fmt:message key="jsp.version.history.column4"/></th>
			<th 
				 id="tt5" class="oddRowEvenCol"><fmt:message key="jsp.version.history.column5"/> </th>
		</tr>
		
		<% for(Version versRow : historyVersions) {  
		
			EPerson versRowPerson = versRow.getEperson();
            String[] identifierPath = UIUtil.getItemIdentifier(UIUtil.obtainContext(request), versRow.getItem());
            String url = identifierPath[0];
            String identifier; // identifier in form of an url (prefixed with an resolver)
            String prefix = (ConfigurationManager.getProperty("webui.identifier.prefix") == null) ? "url" : ConfigurationManager
                    .getProperty("webui.identifier.prefix");
            if (StringUtils.equalsIgnoreCase("stripped", prefix)) {
                identifier = identifierPath[2]; // identifier without any prefix
            } else if (StringUtils.equalsIgnoreCase("prefixed", prefix)) {
                identifier = identifierPath[3]; // identifier prefixed with e.g. doi: or hdl:
            } else {
                // default: "url"
                identifier = identifierPath[1]; // identifier in form of an URL prefixed with a resolver.
            } %>
            <tr>
            <td headers="tt1" class="oddRowEvenCol"><%= versRow.getVersionNumber() %></td>
            <td headers="tt2" class="oddRowOddCol"><a href="<%= url %>"><%= identifier %></a><%= item.getID()==versRow.getItem().getID()?"<span class=\"glyphicon glyphicon-asterisk\"></span>":""%></td>
            <% if(admin_button) { %>
                <td headers="tt3" class="oddRowEvenCol"><a
                    href="mailto:<%= versRowPerson.getEmail() %>"><%=versRowPerson.getFullName() %></a> </td>
            <% }
            else if(item_history_include_submitter) { %>
                <td headers="tt3" class="oddRowEvenCol"> <%=versRowPerson.getFullName() %> </td>
            <% }%>
			<td headers="tt4" class="oddRowOddCol"><%= versRow.getVersionDate() %></td>
			<td headers="tt5" class="oddRowEvenCol"><%= versRow.getSummary() %></td>
		</tr>
		<% } %>
	</table>
	<div class="panel-footer"><fmt:message key="jsp.version.history.legend"/></div>
	</div>
<%
        }
    }
%>
<br/>
    <%-- Create Commons Link --%>
<%
    if (creativecommonslink != "")
    {
%>
    <p class="text-center alert alert-info"><fmt:message key="jsp.display-item.text3"/> <a href="<%= creativecommonslink %>"><fmt:message key="jsp.display-item.license"/></a>
    <a href="<%= creativecommonslink %>"><img src="<%= request.getContextPath() %>/image/cc-somerights.gif" border="0" alt="Creative Commons" style="margin-top: -5px;" class="pull-right"/></a>
    </p>
<%
    } else {
%>
    <p class="text-center alert alert-info"><fmt:message key="jsp.display-item.copyright"/></p>
<%
    } 
%>
	</div>
    
</dspace:layout>
