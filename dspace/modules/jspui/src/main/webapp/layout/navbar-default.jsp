<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>
<%--
  - Default navigation bar
--%>

<%@page import="org.apache.commons.lang.StringUtils"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ page contentType="text/html;charset=UTF-8" %>

<%@ taglib uri="/WEB-INF/dspace-tags.tld" prefix="dspace" %>

<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale"%>
<%@ page import="javax.servlet.jsp.jstl.fmt.LocaleSupport" %>
<%@ page import="org.dspace.core.I18nUtil" %>
<%@ page import="org.dspace.app.webui.util.UIUtil" %>
<%@ page import="org.dspace.app.webui.util.LocaleUIHelper" %>
<%@ page import="org.dspace.content.Collection" %>
<%@ page import="org.dspace.content.Community" %>
<%@ page import="org.dspace.eperson.EPerson" %>
<%@ page import="org.dspace.core.ConfigurationManager" %>
<%@ page import="org.dspace.browse.BrowseIndex" %>
<%@ page import="org.dspace.browse.BrowseInfo" %>
<%@ page import="java.util.Map" %>
<%
    // Is anyone logged in?
    EPerson user = (EPerson) request.getAttribute("dspace.current.user");

    // Is the logged in user an admin
    Boolean admin = (Boolean)request.getAttribute("is.admin");
    boolean isAdmin = (admin == null ? false : admin.booleanValue());

    // Get the current page, minus query string
    String currentPage = UIUtil.getOriginalURL(request);
    int c = currentPage.indexOf( '?' );
    if( c > -1 )
    {
        currentPage = currentPage.substring( 0, c );
    }

    // E-mail may have to be truncated
    String navbarEmail = null;

    if (user != null)
    {
        navbarEmail = user.getEmail();
    }
    
    // get the browse indices
    
	BrowseIndex[] bis = BrowseIndex.getBrowseIndices();
    BrowseInfo binfo = (BrowseInfo) request.getAttribute("browse.info");
    String browseCurrent = "";
    if (binfo != null)
    {
        BrowseIndex bix = binfo.getBrowseIndex();
        // Only highlight the current browse, only if it is a metadata index,
        // or the selected sort option is the default for the index
        if (bix.isMetadataIndex() || bix.getSortOption() == binfo.getSortOption())
        {
            if (bix.getName() != null)
    			browseCurrent = bix.getName();
        }
    }
    
    String extraNavbarData = (String)request.getAttribute("dspace.cris.navbar");
 // get the locale languages
    Locale[] supportedLocales = I18nUtil.getSupportedLocales();
    Locale sessionLocale = UIUtil.getSessionLocale(request);
    boolean isRtl = StringUtils.isNotBlank(LocaleUIHelper.ifLtr(request, "","rtl"));    

    String[] mlinks = new String[0];
    String mlinksConf = ConfigurationManager.getProperty("cris", "navbar.cris-entities");
    if (StringUtils.isNotBlank(mlinksConf)) {
    	mlinks = StringUtils.split(mlinksConf, ",");
    }
    
    boolean showCommList = ConfigurationManager.getBooleanProperty("community-list.show.all",true);
%>
       <div class="navbar-header">
         <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
           <span class="icon-bar"></span>
           <span class="icon-bar"></span>
           <span class="icon-bar"></span>
         </button>
       </div>
       <nav class="collapse navbar-collapse bs-navbar-collapse" role="navigation">
         <ul id="top-menu" class="nav navbar-nav navbar-<%= isRtl ? "right":"left"%>">
           <li class="pull-<%= isRtl ? "right":"left"%>"><a class="navbar-brand" href="<%= request.getContextPath() %>/"><img height="25" src="<%= request.getContextPath() %>/image/dspace-logo-only.png" alt="DSpace logo" /></a></li>

	    <!-- Home/Start -->
	    <li id="home-top-menu" class="pull-<%= isRtl ? "right":"left"%>   <%= currentPage.endsWith("/home.jsp")? 
        	   "active" : "" %>"><a href="<%= request.getContextPath() %>/">
		    <img src=/image/navicons/Uni.svg class="navicon"/></br>
		    <fmt:message key="jsp.layout.navbar-default.home"/></a>
	    </li>
		  <% if(showCommList){ %>

	    <!-- Stöbern -->
           <li class="dropdown">
             <a href="#" class="dropdown-toggle" data-toggle="dropdown"><img src=/image/navicons/Dataset.svg class="navicon"/><br><fmt:message key="jsp.layout.navbar-default.browse"/> <b class="caret"></b></a>
             <ul class="dropdown-menu">
                <li><a href="<%= request.getContextPath() %>/community-list"><fmt:message key="jsp.layout.navbar-default.communities-collections"/></a></li>
                <li><a href="<%= request.getContextPath() %>/handle/11420/2"><fmt:message key="jsp.layout.navbar-default.collection-with-fulltext"/></a></li>
                <li><a href="<%= request.getContextPath() %>/handle/11420/2093"><fmt:message key="jsp.layout.navbar-default.collection-without-fulltext"/></a></li>
                <li><a href="<%= request.getContextPath() %>/handle/11420/2023"><fmt:message key="jsp.layout.navbar-default.collection-researchdata"/></a></li>
<!--
                    <li class="divider"></li>
                    <li class="dropdown-header"><fmt:message key="jsp.layout.navbar-default.browse-items-by"/>:</li>
                    <%-- Insert the dynamic browse indices here --%>
                    <%
                        for (int i = 0; i < bis.length; i++)
                        {
                            BrowseIndex bix = bis[i];
                            String key = "browse.menu." + bix.getName();
                        %>
                        <li><a href="<%= request.getContextPath() %>/browse?type=<%= bix.getName() %>"><fmt:message key="<%= key %>"/></a></li>
                        <%
                        }
                        %>
                   <%-- End of dynamic browse indices --%>
-->
            </ul>
          </li>
<!-- CRIS Community List
		   <li id="communitylist-top-menu" class="<%= currentPage.endsWith("/community-list")? 
        		   "active" : "" %>"><a href="<%= request.getContextPath() %>/community-list"><fmt:message key="jsp.layout.navbar-default.communities-collections"/></a></li>
-->
        		 <% }%> 
           <% for (String mlink : mlinks) { %>
           <c:set var="exploremlink">
           <%= mlink.trim() %>
           </c:set>
           <c:set var="fmtkey">
           jsp.layout.navbar-default.cris.<%= mlink.trim() %>
           </c:set>
           <li id="<%= mlink.trim() %>-top-menu" class="hidden-xs hidden-sm <c:if test="${exploremlink == location}">active</c:if>"><a href="<%= request.getContextPath() %>/cris/explore/<%= mlink.trim() %>"><fmt:message key="${fmtkey}"/></a></li>
           <% } %>
<%--
           <li class="dropdown hidden-md hidden-lg">
             <a href="#" class="dropdown-toggle" data-toggle="dropdown"><fmt:message key="jsp.layout.navbar-default.explore"/> <b class="caret"></b></a>
             <ul class="dropdown-menu">
           <% for (String mlink : mlinks) { %>
           <c:set var="exploremlink">
           <%= mlink.trim() %>
           </c:set>
           <c:set var="fmtkey">
           jsp.layout.navbar-default.cris.<%= mlink.trim() %>
           </c:set>
           <li class="<c:if test="${exploremlink == location}">active</c:if>"><a href="<%= request.getContextPath() %>/cris/explore/<%= mlink.trim() %>"><fmt:message key="${fmtkey}"/></a></li>
           <% } %>
           </ul>
           </li>
--%>
 <%
 if (extraNavbarData != null)
 {
%>
       <%= extraNavbarData %>
<%
 }
%>
          <%-- <li id="help-top-menu" class="<%= ( currentPage.endsWith( "/help" ) ? "active" : "" ) %>"><dspace:popup page="<%= LocaleSupport.getLocalizedMessage(pageContext, \"help.index\") %>"><fmt:message key="jsp.layout.navbar-default.help"/></dspace:popup></li> --%>

	<!-- Publikationen -->
          <li class="dropdown <%= (currentPage.endsWith("/browse") && (request.getParameter("type").equals("dateissued") || request.getParameter("type").equals("documenttype") || request.getParameter("type").equals("journals") || request.getParameter("type").equals("crisseries") || request.getParameter("type").equals("tuhhseries"))) ? "active" : "" %>">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown"><img src=/image/navicons/Publikation.svg class="navicon"/><br><fmt:message key="jsp.layout.navbar-default.tuhh.publications"/> <b class="caret"></b></a>
              <ul class="dropdown-menu">
                  <li class="<%= (currentPage.endsWith("/browse") && request.getParameter("type").equals("dateissued")) ? "active" : "" %>"><a href="<%= request.getContextPath() %>/browse?type=dateissued&rpp=50"><fmt:message key="jsp.layout.navbar-default.tuhh.dateissued"/></a></li>
                  <li class="<%= (currentPage.endsWith("/browse") && request.getParameter("type").equals("documenttype")) ? "active" : "" %>"><a href="<%= request.getContextPath() %>/browse?type=documenttype&rpp=100"><fmt:message key="jsp.layout.navbar-default.tuhh.doctype"/></a></li>
                  <li class="<%= (currentPage.endsWith("/browse") && request.getParameter("type").equals("author")) ? "active" : "" %>"><a href="<%= request.getContextPath() %>/browse?type=author&rpp=50"><fmt:message key="jsp.layout.navbar-default.tuhh.author"/></a></li>
                  <li class="<%= (currentPage.endsWith("/browse") && request.getParameter("type").equals("lcAdvisor")) ? "active" : "" %>"><a href="<%= request.getContextPath() %>/browse?type=lcAdvisor&rpp=50"><fmt:message key="jsp.layout.navbar-default.tuhh.advisor"/></a></li>
                  <li class="<%= (currentPage.endsWith("/browse") && request.getParameter("type").equals("journals")) ? "active" : "" %>"><a href="<%= request.getContextPath() %>/browse?type=journals&rpp=50"><fmt:message key="jsp.layout.navbar-default.tuhh.journals"/></a></li>
                  <li class="<%= (currentPage.endsWith("/browse") && request.getParameter("type").equals("crisseries")) ? "active" : "" %>"><a href="<%= request.getContextPath() %>/browse?type=crisseries&rpp=100"><fmt:message key="jsp.layout.navbar-default.tuhh.crisseries"/></a></li>
                  <li class="<%= (currentPage.endsWith("/browse") && request.getParameter("type").equals("tuhhseries")) ? "active" : "" %>"><a href="<%= request.getContextPath() %>/browse?type=tuhhseries&rpp=50"><fmt:message key="jsp.layout.navbar-default.tuhh.tuhhseries"/></a></li>
              </ul>
          </li>
<!--
	<!-- Projekte --
          <li class="<%= (currentPage.endsWith("/browse") && request.getParameter("type").equals("pjtitle")) ? "active" : "" %>"><a href="<%= request.getContextPath() %>/browse?type=pjtitle&rpp=50"><img src=/image/navicons/Projekt.svg class="navicon"/><br><fmt:message key="jsp.layout.navbar-default.tuhh.projects"/></a></li>

	<!-- Forschungsdaten --
          <li class="<%= (currentPage.endsWith("/browse") && request.getParameter("type").equals("researchdata")) ? "active" : "" %>"><a href="<%= request.getContextPath() %>/browse?type=researchdata&rpp=50"><img src=/image/navicons/Dataset.svg class="navicon"/><br><fmt:message key="jsp.layout.navbar-default.tuhh.researchdata"/></a></li>

	<!-- TUHH-Mitarbeiter/Institute als Dropdown
          <li class="dropdown <%= (currentPage.endsWith("/browse") && (request.getParameter("type").equals("rpname") || request.getParameter("type").equals("rpdept"))) ? "active" : "" %>">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown"><img src=/image/navicons/Gruppe.svg class="navicon"/><br><fmt:message key="jsp.layout.navbar-default.tuhh.researcherprofiles"/> <b class="caret"></b></a>
              <ul class="dropdown-menu">
                  <li class="<%= (currentPage.endsWith("/browse") && request.getParameter("type").equals("rpname")) ? "active" : "" %>"><a href="<%= request.getContextPath() %>/browse?type=rpname&rpp=50"><fmt:message key="jsp.layout.navbar-default.tuhh.researcherprofilesbyname"/></a></li>
                  <li class="<%= (currentPage.endsWith("/browse") && request.getParameter("type").equals("rpdept")) ? "active" : "" %>"><a href="<%= request.getContextPath() %>/browse?type=rpdept&rpp=100"><fmt:message key="jsp.layout.navbar-default.tuhh.researcherprofilesbydept"/></a></li>
              </ul>
          </li>
        -->

	<!-- TUHH-Mitarbeiter/Institute -->
          <li class="<%= (currentPage.endsWith("/browse") && (request.getParameter("type").equals("rpname"))) ? "active" : "" %>">
              <a href="<%= request.getContextPath() %>/browse?type=rpname&rpp=50"><img src=/image/navicons/Gruppe.svg class="navicon"/><br><fmt:message key="jsp.layout.navbar-default.tuhh.researcherprofiles"/></a>
          </li>

	<!-- TUHH Institute -->
          <li class="<%= (currentPage.endsWith("/browse") && request.getParameter("type").equals("ouname")) ? "active" : "" %>"><a href="<%= request.getContextPath() %>/browse?type=ouname&rpp=100"><img src=/image/navicons/Department.svg class="navicon"/><br><fmt:message key="jsp.layout.navbar-default.tuhh.orgunits"/></a></li>

          <!-- Hilfe? -->
          <li class="dropdown">
             <a href="#" class="dropdown-toggle" data-toggle="dropdown"><img src=/image/navicons/Info.svg class="navicon"/><br><fmt:message key="jsp.layout.navbar-default.help"/>  <b class="caret"></b></a>
             <ul class="dropdown-menu">
<%--                <li><fmt:message key="jsp.layout.navbar-default.tor.help"/></li>            --%>
                <li><fmt:message key="jsp.layout.navbar-default.tubdok-contact"/></li>
                <li><fmt:message key="jsp.layout.navbar-default.tubdok-depositlicense"/></li>
                <li class="divider"></li>
                <li><fmt:message key="jsp.layout.navbar-default.tore-help"/></li>
<%--                <li><a href="<%= request.getContextPath() %>/staticpages.jsp?incFile=1"><fmt:message key="jsp.layout.navbar-default.tubdok-policies"/></a></li>               --%>
<%--                <li><a href="<%= request.getContextPath() %>/staticpages.jsp?incFile=6"><fmt:message key="jsp.layout.navbar-default.tubdok-usage"/></a></li>          --%>
<%--                <li class="divider"></li>                                                                                --%>
<%--                <li class="dropdown-header"><fmt:message key="jsp.layout.navbar-default.tubdok-diss"/></li>              --%>
<%--                <li><a href="<%= request.getContextPath() %>/staticpages.jsp?incFile=7"><fmt:message key="jsp.layout.navbar-default.tubdok-dissnote"/></a></li>        --%>
                <li><fmt:message key="jsp.layout.navbar-default.tubdok-publnote"/></li>
<%--                <li class="divider"></li> --%>
<%--                <li><a href="<%= request.getContextPath() %>/staticpages.jsp?incFile=3"><fmt:message key="jsp.layout.navbar-default.tubdok-documentation"/></a></li>         --%>
<%--                <li><a href="<%= request.getContextPath() %>/staticpages.jsp?incFile=4"><fmt:message key="jsp.layout.navbar-default.tubdok-disclaimer"/></a></li>         --%>
                <li><fmt:message key="jsp.layout.navbar-default.tubdok-orcid"/></li>
<%--
                <li class="divider"></li>
                <li><fmt:message key="jsp.layout.navbar-default.tubdok-impress"/></li>
--%>
             </ul>
          </li>

	<!-- Veröffentlichen -->
          <li class="<%= ( currentPage.endsWith( "/submit" ) ? "active" : "" ) %>"><a href="<%= request.getContextPath() %>/submit"><img src=/image/navicons/Publikation.svg class="navicon"/><br><fmt:message key="jsp.layout.navbar-default.publish"/></a></li>
       </ul>

 <%-- if (supportedLocales != null && supportedLocales.length > 1)
     {
 
    <div class="nav navbar-nav navbar-<%= isRtl ? "left" : "right" %>">
	 <ul class="nav navbar-nav navbar-<%= isRtl ? "left" : "right" %>">
      <li id="language-top-menu" class="dropdown">
       <a href="#" class="dropdown-toggle" data-toggle="dropdown"><fmt:message key="jsp.layout.navbar-default.language"/><b class="caret"></b></a>
        <ul class="dropdown-menu">
 <%
    for (int i = supportedLocales.length-1; i >= 0; i--)
     {
 %>
      <li>
        <a onclick="javascript:document.repost.locale.value='<%=supportedLocales[i].toString()%>';
                  document.repost.submit();" href="?locale=<%=supportedLocales[i].toString()%>">
          <%= LocaleSupport.getLocalizedMessage(pageContext, "jsp.layout.navbar-default.language."+supportedLocales[i].toString()) %>                  
     
       </a>
      </li>
 <%
     }
 %>
     </ul>
    </li>
    </ul>
  </div>
 <%
   }
 %>
 --%>
       <div class="nav navbar-nav navbar-<%= isRtl ? "left" : "right" %>">
		<ul class="nav navbar-nav navbar-<%= isRtl ? "left" : "right" %>">
         <li class="dropdown">
         <%
    if (user != null)
    {
		%>
		<li id="userloggedin-top-menu" class="dropdown">
		<a href="#" class="dropdown-toggle <%= isRtl ? "" : "text-right" %>" data-toggle="dropdown"><span class="glyphicon glyphicon-user"></span><br><fmt:message key="jsp.layout.navbar-default.loggedin">
		      <fmt:param><%= StringUtils.abbreviate(navbarEmail, 20) %></fmt:param>
		  </fmt:message> <b class="caret"></b></a>
		<%
    } else {
		%>
			<li id="user-top-menu" class="dropdown">
             <a href="#" class="dropdown-toggle" data-toggle="dropdown"><span class="glyphicon glyphicon-user"></span><br><fmt:message key="jsp.layout.navbar-default.sign"/> <b class="caret"></b></a>
	<% } %>             
             <ul class="dropdown-menu">
               <li><a href="<%= request.getContextPath() %>/mydspace"><fmt:message key="jsp.layout.navbar-default.users"/></a></li>
<!--               <li><a href="<%= request.getContextPath() %>/subscribe"><fmt:message key="jsp.layout.navbar-default.receive"/></a></li> -->
<!--               <li><a href="<%= request.getContextPath() %>/profile"><fmt:message key="jsp.layout.navbar-default.edit"/></a></li> -->

		<%
		  if (isAdmin)
		  {
		%>
			   <li class="divider"></li>  
               <li><a href="<%= request.getContextPath() %>/dspace-admin"><fmt:message key="jsp.administer"/></a></li>
		<%
		  }
		  if (user != null) {
		%>
		<li><a href="<%= request.getContextPath() %>/logout"><span class="glyphicon glyphicon-log-out"></span> <fmt:message key="jsp.layout.navbar-default.logout"/></a></li>
		<% } %>
             </ul>
           </li>
          </ul>

	</div>
    </nav>
