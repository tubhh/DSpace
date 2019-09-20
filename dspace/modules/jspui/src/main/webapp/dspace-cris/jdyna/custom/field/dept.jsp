<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/sql" prefix="sql"%>
<%@ taglib uri="researchertags" prefix="researcher"%>
<%@ taglib uri="jdynatags" prefix="dyna"%>
<%@ taglib uri="http://ajaxtags.org/tags/ajax" prefix="ajax"%>

<%@ page import="org.dspace.core.ConfigurationManager" %>
<%@ page import="org.apache.commons.lang.StringUtils"%>
<%@ page import="org.dspace.app.webui.util.UIUtil" %>
<%@ page import="java.util.Locale"%>

<% 
	Locale sessionLocale = UIUtil.getSessionLocale(request);
	String currLocale = null;
	if (sessionLocale != null) {
		currLocale = sessionLocale.toString();
	}

	String supportedLocales = ConfigurationManager.getProperty("webui.supported.locales");
	String[] msupportedLocales = new String[0];
	if (StringUtils.isNotBlank(supportedLocales)) {
	    msupportedLocales = StringUtils.split(supportedLocales, ",");
	}
	
%>

<c:forEach var="value" items="${anagraficaObject.anagrafica4view['dept']}" varStatus="valueStatus">
			
			<c:if test="${valueStatus.count != 1}"><br/></c:if>
			<c:set var="displayObject" value="${value.value.real}" />
			<c:set var="metadataRequest"><%= "${displayObject.anagrafica4view['"+"name_" + currLocale+"'][0].value}" %></c:set>
			<c:set var="displayPointerRequest" value="${dyna:getDisplayValue(displayObject,metadataRequest)}" />
			<c:set var="renderingUrlPath" value="${tipologiaDaVisualizzare.rendering.urlPath}" />

			<div class="dynaField" style="min-width:5em;">
                        <span class="dynaLabel" style="width:15em;"><fmt:message key="rp.affiliation.label" /></span>
                        <div id="instituteDiv" class="dynaFieldValue">
<span style="min-width: 30em;">
			<c:choose>
				<c:when test="${!empty displayPointerRequest}">
					<c:choose>				
						<c:when test="${!empty renderingUrlPath}">						
							<a href="${root}/${dyna:getDisplayValue(displayObject,renderingUrlPath)}">${displayPointerRequest}</a>
						</c:when>
						<c:otherwise>
							${displayPointerRequest}
						</c:otherwise>
					</c:choose>					
				</c:when>
				<c:otherwise>
					<c:set var="displayPointer" value="${dyna:getDisplayValue(displayObject,tipologiaDaVisualizzare.rendering.display)}" />				
					<c:choose>				
						<c:when test="${!empty displayPointer}">
							<c:choose>				
								<c:when test="${!empty renderingUrlPath}">						
									<a href="${root}/${dyna:getDisplayValue(displayObject,renderingUrlPath)}">${displayPointer}</a>
								</c:when>
								<c:otherwise>
									${displayPointer}
								</c:otherwise>
							</c:choose>
						</c:when>
						<c:otherwise>
						<% for(String mm : msupportedLocales) { %>
							<c:set var="metadatalocalized"><%= "${displayObject.anagrafica4view['"+"name_" + mm+"'][0].value}" %></c:set>
							<c:set var="displayPointerLocalized" value="${dyna:getDisplayValue(displayObject,metadatalocalized)}" />
							<c:choose>
								<c:when test="${!empty renderingUrlPath}">						
									<a href="${root}/${dyna:getDisplayValue(displayObject,renderingUrlPath)}">${displayPointerLocalized}</a>
								</c:when>
								<c:otherwise>
									${displayPointerLocalized}
								</c:otherwise>
							</c:choose>
						<% } %>
						</c:otherwise>
					</c:choose>
				</c:otherwise>
			</c:choose>
</span>
    </div>
    </div>
</c:forEach>

<br/>

