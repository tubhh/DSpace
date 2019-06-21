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
  -    submitter_buztton - Boolen show submitter 'new version' button
  --%>
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
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="org.dspace.app.webui.servlet.MyDSpaceServlet"%>
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
    String doiMetadata = ConfigurationManager.getProperty("cris","ametrics.identifier.doi");
    String isbnMetadata = ConfigurationManager.getProperty("cris","ametrics.identifier.isbn");
    String pmidMetadata = ConfigurationManager.getProperty("cris","ametrics.identifier.pmid");
    if (doiMetadata == null) {
    	doiMetadata = "dc.identifier.doi";
    }
    if (isbnMetadata == null) {
    	isbnMetadata = "dc.identifier.isbn";
    }
    if (pmidMetadata == null) {
    	pmidMetadata = "dc.identifier.pmid";
    }
    String doi = item.getMetadata(doiMetadata);
    String isbn = item.getMetadata(isbnMetadata);
    String pmid = item.getMetadata(pmidMetadata);
    boolean scholarEnabled = ConfigurationManager.getBooleanProperty("cris","ametrics.google.scholar.enabled",false);
    boolean altMetricEnabled = ConfigurationManager.getBooleanProperty("cris","ametrics.altmetric.enabled",false) && (StringUtils.isNotBlank(doi) || StringUtils.isNotBlank(isbn));
    boolean altMetricDimensionsEnabled = ConfigurationManager.getBooleanProperty("cris","ametrics.altmetric.dimensionsbadges.enabled",false) && (StringUtils.isNotBlank(doi) || StringUtils.isNotBlank(pmid));
    
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
    
    VersionHistory history = (VersionHistory)request.getAttribute("versioning.history");
    List<Version> historyVersions = (List<Version>)request.getAttribute("versioning.historyversions");
    
  	String crisID = (String)request.getAttribute("crisID");
%>

<script type='text/javascript' src='<%= request.getContextPath() %>/static/js/abbreviatetext.js'></script>

<% if(pmcEnabled || scopusEnabled || wosEnabled || scholarEnabled || altMetricEnabled) { %>
<c:set var="dspace.layout.head.last" scope="request">
<% if(altMetricEnabled) { %> 
<script type='text/javascript' src='https://d1bxh8uas1mnw7.cloudfront.net/assets/embed.js'></script>
<% } %>
<script type="text/javascript"><!--
j(document).ready(function() {

	<% 
		if(StringUtils.isNotBlank(crisID)) {
	%>
		j.ajax({
			url : "<%=request.getContextPath()%>/json/checkclaimpublicationmetadata",
			data : {																			
				"item" : "<%= item.getID()%>",
				"crisid": "<%= crisID %>"
			},
			success : function(data) {
					j.each(data, function( index, value ) {
						j('#claim-usertools').append("<a class=\"btn btn-primary col-md-12\" href=\"<%= request.getContextPath() %>/tools/claim?action=" + value.action + "&metadata=" + value.metadata + "&handle=<%= handle %>\">" + value.message + "</a>");	
					});				
			},
			error : function(data) {
			}
		});	
	<%
		}
	%>
});
--></script>
<dspace:layout title="<%= title %>">
<%
    if (handle != null)
    {
%>

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
                <div class="well"><fmt:message key="jsp.display-item.identifier"/>
                <code><%= preferredIdentifier %></code></div>
<%
        if (admin_button)  // admin edit button
        { %>
        <dspace:sidebar>
            <div class="panel panel-warning">
            	<div class="panel-heading"><fmt:message key="jsp.admintools"/></div>
            	<div class="panel-body">
                <form method="get" action="<%= request.getContextPath() %>/tools/edit-item">
                    <input type="hidden" name="item_id" value="<%= item.getID() %>" />
                    <%--<input type="submit" name="submit" value="Edit...">--%>
                    <input class="btn btn-default col-md-12" type="submit" name="submit" value="<fmt:message key="jsp.general.edit.button"/>" />
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
        </dspace:sidebar>
<%      } %>

<%
        // submitter create new version button
        if (submitter_button && hasVersionButton) {
%>
        <dspace:sidebar>
            <div class="panel panel-warning">
                <div class="panel-heading"><fmt:message key="jsp.submittertools"/></div>
                <div class="pannel-body">
                    <form method="get" action="<%= request.getContextPath()%>/tools/version">
                        <input type="hidden" name="itemID" value="<%= item.getID()%>" />
                        <input class="btn btn-default col-md-12" type="submit" name="submit" value="<fmt:message key="jsp.general.version.button"/>" />
                    </form>
                </div>
            </div>
        </dspace:sidebar>
<%
        }
    }

    String displayStyle = (displayAll ? "full" : "");
%>
    <dspace:item-preview item="<%= item %>" />
    <dspace:item item="<%= item %>" collections="<%= collections %>" style="<%= displayStyle %>" />
<div class="container row">
<%
    String locationLink = request.getContextPath() + "/handle/" + handle;

    if (displayAll)
    {
%>
<%
        if (workspace_id != null)
        {
%>
    <form class="col-md-2" method="post" action="<%= request.getContextPath() %>/view-workspaceitem">
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
%>
<%
    }
    else
    {
%>
<%
        if (workspace_id != null)
        {
%>
    <form class="col-md-2" method="post" action="<%= request.getContextPath() %>/view-workspaceitem">
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
   <form class="col-md-2" method="post" action="<%= request.getContextPath() %>/workspace">
        <input type="hidden" name="workspace_id" value="<%= workspace_id.intValue() %>"/>
        <input class="btn btn-primary" type="submit" name="submit_open" value="<fmt:message key="jsp.display-item.back_to_workspace"/>"/>
    </form>
<%
    } else {

		if (suggestLink)
        {
%>
    <a class="btn btn-success" href="<%= request.getContextPath() %>/suggest?handle=<%= handle %>" target="new_window">
       <fmt:message key="jsp.display-item.suggest"/></a>
<%
        }
%>
    <a class="statisticsLink  btn btn-primary" href="<%= request.getContextPath() %>/handle/<%= handle %>/statistics"><fmt:message key="jsp.display-item.display-statistics"/></a>
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
      		<div class='altmetric-embed' data-hide-no-mentions="true" data-badge-popover="right" data-badge-type="donut" data-link-target='_blank'
      		<% if (doi != null) { %> data-doi="<%= doi %>"<% } else if (isbn != null) { %> data-isbn="<%= isbn %>"<% } %>></div>
	</div>
	<div class="media-body media-middle text-center">
		<h4 class="media-heading"><fmt:message key="jsp.display-item.citation.altmetric"/></h4>
	</div>
</div>
</div>
<% } 
  if(altMetricDimensionsEnabled) { %>
<div class="col-lg-12 col-md-4 col-sm-6">
<div class="dimensions">
	<div class="media-left">
	
      	<div class="__dimensions_badge_embed__" data-legend="hover-right" data-style="small_circle" <% if (doi != null) { %> data-doi="<%= doi %>"<% } else if (pmid != null) { %> data-pmid="<%= pmid %>"<% } %>" ></div>
      	<script async src="https://badge.dimensions.ai/badge.js" charset="utf-8"></script>
	</div>
	<div class="media-body media-middle text-center">
		<h4 class="media-heading"><fmt:message key="jsp.display-item.citation.altmetric"/></h4>
	</div>
</div>
</div>
<% } %>
    </div>
</div>
<% if(pmcEnabled) { %>
<div class="modal fade" id="dialogPMC" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">

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
    }
%>

	<% if(StringUtils.isNotBlank(crisID)) { %>
	
	       <div class="col-sm-5 col-md-4 col-lg-3">
            <div class="panel panel-warning">
            	<div class="panel-heading"><fmt:message key="jsp.usertools"/></div>
            	<div class="panel-body">
			    	<div id="claim-usertools">
			    	</div>
            	</div>
            </div>
            </div>
    <% } %>
    
</div>
</div>
<br/>
    <%-- Versioning table --%>
<%
    if (versioningEnabled && hasVersionHistory)
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
            }		%>
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
    if (cc_url != null)
    {
%>
    <p class="submitFormHelp alert alert-info"><fmt:message key="jsp.display-item.text3"/> <a href="<%= cc_url %>"><fmt:message key="jsp.display-item.license"/></a>
    <a href="<%= cc_url %>"><img src="<%= request.getContextPath() %>/image/cc-somerights.gif" border="0" alt="Creative Commons" style="margin-top: -5px;" class="pull-right"/></a>
    </p>
    <!--
    <%= cc_rdf %>
    -->
<%
    } else {
%>
    <p class="submitFormHelp alert alert-info"><fmt:message key="jsp.display-item.copyright"/></p>
<%
    } 
%>    
</dspace:layout>
