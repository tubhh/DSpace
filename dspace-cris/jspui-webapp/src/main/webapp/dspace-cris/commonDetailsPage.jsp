<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    https://github.com/CILEA/dspace-cris/wiki/License

--%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<%@ taglib uri="jdynatags" prefix="dyna"%>
<%@ taglib uri="researchertags" prefix="researcher"%>

<%@page import="it.cilea.osd.jdyna.components.IComponent"%>
<%@page import="org.dspace.app.webui.cris.components.ASolrConfigurerComponent"%>
<%@page import="java.util.Map" %>
<%@page import="org.dspace.core.ConfigurationManager" %>
<%@page import="org.dspace.app.cris.model.ACrisObject" %>	
<%@page import="it.cilea.osd.jdyna.components.IComponent"%>

<% 
	ACrisObject entity = (ACrisObject)request.getAttribute("entity");
	Map<String, IComponent> mapInfo = ((Map<String, IComponent>)(request.getAttribute("components"))); 
	boolean showBadgeCount = ConfigurationManager.getBooleanProperty("cris", "webui.tab.show.count.for.firstcomponent", false);
%>
	
	<div id="tabs">
		<ul>
					<c:forEach items="${tabList}" var="area" varStatus="rowCounter">
						<c:set var="tablink"><c:choose>
							<c:when test="${rowCounter.count == 1}">${root}/cris/${specificPartPath}/${authority}</c:when>
							<c:otherwise>${root}/cris/${specificPartPath}/${authority}/${area.shortName}.html</c:otherwise>
						</c:choose></c:set>

						<c:choose>
							<c:when test="${(researcher:isTabHidden(entity,area.shortName) == false)}">
								<c:set var="tabName" value="researcher-menu-item"/>
							</c:when>
							<c:otherwise>
								<c:set var="tabName" value="researcher-menu-item-hidden"/>
							</c:otherwise>
						</c:choose>

						<li data-tabname="${area.shortName}" class="${tabName}" id="bar-tab-${area.id}">
						<c:choose>
							<c:when test="${area.id == tabId}">
								<a href="#tab-${area.id}">
								<c:if test="${!empty area.ext}">
								<img style="width: 16px;vertical-align: middle;" border="0" 
									src="<%=request.getContextPath()%>/cris/researchertabimage/${area.id}" alt="icon" />
								</c:if>	
								<spring:message code="${entity.class.simpleName}.tab.${area.shortName}.label" text="${area.title}"></spring:message>
								<% if(showBadgeCount) { %>
								<c:set var="firstComponentFound" value="false"/>
								<c:forEach items="${area.mask}" var="box" varStatus="boxRowCounter">
								<c:if test="${!empty box.externalJSP && !firstComponentFound}"> 
								<%  
								if(mapInfo!=null && !mapInfo.isEmpty()) {
																		
									for(String key : mapInfo.keySet()) {
								%>
								<c:set var="key"><%= key %></c:set>
								<c:if test="${box.getShortName() eq key && !firstComponentFound}">
								<%			
										ASolrConfigurerComponent iii = (ASolrConfigurerComponent)(mapInfo.get(key));
										String type = (String)iii.getType(request, entity.getId());
									    long count = iii.count(request, type, entity.getId());
										if(count>0) {
								%>
										<span class="badge badge-primary badge-pill"><%= count %></span>
										<c:set var="firstComponentFound" value="true"/>
							    <% 		
										} %>
								</c:if>		
							    <%
									}
								}
								%>
								</c:if>
								</c:forEach>
								<% } %>
								</a>
								
							</c:when>
							<c:otherwise>
									<a href="${tablink}">
									<c:if test="${!empty area.ext}">
									<img style="width: 16px;vertical-align: middle;" border="0"
										src="<%=request.getContextPath()%>/cris/researchertabimage/${area.id}"
			    						alt="icon" />
			    					</c:if>	
			    					<spring:message code="${entity.class.simpleName}.tab.${area.shortName}.label" text="${area.title}"></spring:message>
								<% if(showBadgeCount) { %>
								<c:set var="firstComponentFound" value="false"/>
								<c:forEach items="${area.mask}" var="box" varStatus="boxRowCounter">
								<c:if test="${!empty box.externalJSP && !firstComponentFound}"> 
								<%  
								if(mapInfo!=null && !mapInfo.isEmpty()) {
																		
									for(String key : mapInfo.keySet()) {
								%>
								<c:set var="key"><%= key %></c:set>
								<c:if test="${box.getShortName() eq key && !firstComponentFound}">
								<%			
										ASolrConfigurerComponent iii = (ASolrConfigurerComponent)(mapInfo.get(key));
										String type = (String)iii.getType(request, entity.getId());
									    long count = iii.count(request, type, entity.getId());
										if(count>0) {
								%>
										<span class="badge badge-primary badge-pill"><%= count %></span>
										<c:set var="firstComponentFound" value="true"/>
							    <% 		
										} %>
								</c:if>		
							    <%
									}
								}
								%>
								</c:if>
								</c:forEach>
								<% } %>
			    					</a>
							</c:otherwise>
						</c:choose></li>

					</c:forEach>
		</ul>
	

<c:forEach items="${tabList}" var="areaIter" varStatus="rowCounter">
	<c:if test="${areaIter.id == tabId}">
	<c:set var="area" scope="request" value="${areaIter}"></c:set>
	<jsp:include page="singleTabDetailsPage.jsp"></jsp:include>
	</c:if>
	
</c:forEach>

</div>
<div class="clearfix">&nbsp;</div>