<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>
<%--
  - HTML header for main home page
  --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.dspace.org/dspace-tags.tld" prefix="dspace" %>

<%@ page contentType="text/html;charset=UTF-8" %>

<%@ page import="java.util.List"%>
<%@ page import="java.util.Enumeration"%>
<%@ page import="org.dspace.app.webui.util.JSPManager" %>
<%@ page import="org.dspace.core.ConfigurationManager" %>
<%@ page import="org.dspace.app.util.Util" %>
<%@ page import="javax.servlet.jsp.jstl.core.*" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<%@ page import="java.util.Locale"%>
<%@ page import="org.dspace.core.I18nUtil" %>
<%@ page import="org.dspace.app.webui.util.UIUtil" %>

<%
    Locale[] supportedLocales = I18nUtil.getSupportedLocales();
    Locale sessionLocale = UIUtil.getSessionLocale(request);

    String title = (String) request.getAttribute("dspace.layout.title");
    String navbar = (String) request.getAttribute("dspace.layout.navbar");
    boolean locbar = ((Boolean) request.getAttribute("dspace.layout.locbar")).booleanValue();

    String siteName = ConfigurationManager.getProperty("dspace.name");
    String feedRef = (String)request.getAttribute("dspace.layout.feedref");
    boolean osLink = ConfigurationManager.getBooleanProperty("websvc.opensearch.autolink");
    String osCtx = ConfigurationManager.getProperty("websvc.opensearch.svccontext");
    String osName = ConfigurationManager.getProperty("websvc.opensearch.shortname");
    List parts = (List)request.getAttribute("dspace.layout.linkparts");
    String extraHeadData = (String)request.getAttribute("dspace.layout.head");
    String extraHeadDataLast = (String)request.getAttribute("dspace.layout.head.last");
    String dsVersion = Util.getSourceVersion();
    String generator = dsVersion == null ? "DSpace" : "DSpace "+dsVersion;
    String analyticsKey = ConfigurationManager.getProperty("jspui.google.analytics.key");
%>

<!DOCTYPE html>
<html>
    <head>
        <title><%= title %> | <%= siteName %></title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="Generator" content="<%= generator %>" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="shortcut icon" href="<%= request.getContextPath() %>/favicon.ico" type="image/x-icon"/>
	    <link rel="stylesheet" href="<%= request.getContextPath() %>/static/css/jquery-ui-1.10.3.custom/redmond/jquery-ui-1.10.3.custom.css" type="text/css" />
	    <link rel="stylesheet" href="<%= request.getContextPath() %>/static/css/bootstrap/bootstrap.min.css" type="text/css" />
	    <link rel="stylesheet" href="<%= request.getContextPath() %>/static/css/bootstrap/bootstrap-theme.min.css" type="text/css" />
   	    <link href="<%= request.getContextPath() %>/static/css/font-awesome/css/font-awesome.min.css" rel="stylesheet">
		<link href="<%= request.getContextPath() %>/static/css/jstree/themes/default/style.min.css" rel="stylesheet"/>
	    <link rel="stylesheet" href="<%= request.getContextPath() %>/static/css/bootstrap/dspace-theme.css" type="text/css" />
	    <link rel="stylesheet" href="<%= request.getContextPath() %>/static/css/number-polyfill.css" type="text/css" />
            <link rel="stylesheet" href="<%= request.getContextPath() %>/css/tubhh.css" type="text/css" />
<%
    if (!"NONE".equals(feedRef))
    {
        for (int i = 0; i < parts.size(); i+= 3)
        {
%>
        <link rel="alternate" type="application/<%= (String)parts.get(i) %>" title="<%= (String)parts.get(i+1) %>" href="<%= request.getContextPath() %>/feed/<%= (String)parts.get(i+2) %>/<%= feedRef %>"/>
<%
        }
    }
    
    if (osLink)
    {
%>
        <link rel="search" type="application/opensearchdescription+xml" href="<%= request.getContextPath() %>/<%= osCtx %>description.xml" title="<%= osName %>"/>
<%
    }

    if (extraHeadData != null)
        { %>
<%= extraHeadData %>
<%
        }
%>
	<script type='text/javascript' src="<%= request.getContextPath() %>/static/js/jquery/jquery-1.11.3.min.js"></script>
	<script type='text/javascript' src='<%= request.getContextPath() %>/static/js/jquery/jquery-ui-1.11.4.min.js'></script>
	<script type='text/javascript' src='<%= request.getContextPath() %>/static/js/bootstrap/bootstrap.min.js'></script>
	<script type="text/javascript" src="<%= request.getContextPath() %>/js/tmpl.min.js"></script>
	<script type="text/javascript" src="<%= request.getContextPath() %>/js/jquery.dataTables.min.js"></script>
	<script type="text/javascript" src="<%= request.getContextPath() %>/js/dataTables.bootstrap.min.js"></script>
	<script type="text/javascript" src="<%= request.getContextPath() %>/js/dataTables.buttons.min.js"></script>
	<script type="text/javascript" src="<%= request.getContextPath() %>/js/buttons.bootstrap.min.js"></script>
	<script type="text/javascript" src="<%= request.getContextPath() %>/js/buttons.html5.min.js"></script>
	<script type="text/javascript" src="<%= request.getContextPath() %>/js/dataTables.responsive.min.js"></script>
	<script type='text/javascript' src='<%= request.getContextPath() %>/static/js/holder.js'></script>
	<script type="text/javascript" src="<%= request.getContextPath() %>/js/jdyna/jdyna.js"></script>
	<script type="text/javascript" src="<%= request.getContextPath() %>/utils.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/static/js/choice-support.js"></script>    
	<script type='text/javascript'>
		var j = jQuery.noConflict();
		var $ = jQuery.noConflict();
		var JQ = j;
		dspaceContextPath = "<%=request.getContextPath()%>";
	</script>
	
	<script type='text/javascript'
		src='<%= request.getContextPath() %>/js/dedup.js'></script>
	<script type='text/javascript'
		src='<%= request.getContextPath() %>/js/dedup-function.js'></script>
	<script type='text/javascript'
		src='<%= request.getContextPath() %>/js/dedup-behaviour.js'></script>
			
    <%--Gooogle Analytics recording.--%>
    <%
    if (analyticsKey != null && analyticsKey.length() > 0)
    {
    %>
        <script type="text/javascript">
            var _gaq = _gaq || [];
            _gaq.push(['_setAccount', '<%= analyticsKey %>']);
            _gaq.push(['_trackPageview']);

            (function() {
                var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
                ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
                var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
            })();
        </script>
    <%
    }
    if (extraHeadDataLast != null)
    { %>
		<%= extraHeadDataLast %>
		<%
		    }
    %>
    

<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
  <script src="<%= request.getContextPath() %>/static/js/html5shiv.js"></script>
  <script src="<%= request.getContextPath() %>/static/js/respond.min.js"></script>
<![endif]-->
    </head>

    <%-- HACK: leftmargin, topmargin: for non-CSS compliant Microsoft IE browser --%>
    <%-- HACK: marginwidth, marginheight: for non-CSS compliant Netscape browser --%>
    <body class="undernavigation">
<a class="sr-only" href="#content">Skip navigation</a>
<div id="page">
<header id="branding">

            <div id="tulogo">
    <a href="http://www.tuhh.de">
    <img src="/image/TUHH-Logo.png" alt="TUHH HOME" title="TUHH HOME" />
                </a>
            </div><!-- logo -->
<div id="tulogosmall">
<a id="box-link" href="http://www.tuhh.de"></a>
</div>
<div id="tubdoklogomobile">
<a id="box-link" href="https://tubdok.tub.tuhh.de"></a>
</div>
            <div id="apbranding-box">
                <div class="sitename">
            <a style="color:#000000;" href="https://tubdok.tub.tuhh.de/"
            title="<fmt:message key="tuhh.library.name" />" rel="home">
            <fmt:message key="tuhh.library.dokuservice.description" /></a>
                </div>
        <div class="tubdoklogosmall">
    <a id="box-link" href="https://tubdok.tub.tuhh.de"> </a>
    </div>
<div> <!-- 3 Spalten für DE|EN Search Logo -->
                <div class="msls">
<% if (supportedLocales != null && supportedLocales.length > 1)
{
%>
        <form method="get" name="langrepost" action="">
          <input type ="hidden" name ="locale"/>
          <!-- hidden field type will cause trouble if it is null, so use a different name and rename field if necessary -->
          <input type ="hidden" name ="subst_type"/>
        </form>
<%
for (int i = supportedLocales.length-1; i >= 0; i--)
{
%>
        <a class ="langChange"
                  onclick="javascript:document.langrepost.locale.value='<%=supportedLocales[i].toString()%>';
                  var t = '<%=request.getParameter("type")%>';
                  if (t != null) {
                      document.langrepost.subst_type.value=t;
                      document.langrepost.subst_type.setAttribute('name', 'type');
                  }
                  document.langrepost.submit();">
                 <%= supportedLocales[i].getDisplayLanguage(supportedLocales[i])%>
        </a> &nbsp;
<%
}
}
%>
            </div>
        <div class="tubdoksearch">
        <%-- Search Box DSpace --%>
        <form method="get" action="<%= request.getContextPath() %>/simple-search" class="navbar-right" scope="search" accept-charset="UTF-8">
            <div class="form-group">
                <input type="text" class="form-control" placeholder="<fmt:message key="jsp.layout.navbar-default.search"/>" name="query" id="tequery" size="25"/>
                <%-- <button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span></button> --%>
            </div>
        </form>
        <%-- Search Box TUBfind --%>
        <%--
        <form method="get" action="https://katalog.tub.tu-harburg.de/Search/Results/" class="navbar-right" scope="search" accept-charset="UTF-8" target="_blank">
            <div class="form-group">
                <input type="text" class="form-control" placeholder="<fmt:message key="jsp.layout.navbar-default.search"/>" name="lookfor" id="tequery" size="25"/>
            </div>
            <input type="hidden" value="TUBdok" name="shard[]">
            <input type="hidden" value="TUBdok" name="type">
        </form>
        --%>
        </div>
        <div class="tubdoklogo">
    <a href="https://tubdok.tub.tuhh.de">
        <img src="/image/tub_dok_logo.png"  height="40" alt="TUBdok" title="TUBdok Home" />
            </a>
    </div>
        </div> <!-- 3 Spalten ende-->

                <div class="clear"></div>
            </div><!-- branding-box -->

<div class="clear"></div>






















<header class="navbar navbar-inverse navbar-square">
    <%
    if (!navbar.equals("off"))
    {
%>
            <div class="container-fluid">
                <dspace:include page="<%= navbar %>" />
            </div>
<%
    }
    else
    {
    	%>
        <div class="container-fluid">
            <dspace:include page="/layout/navbar-minimal.jsp" />
        </div>
<%    	
    }
%>
</header>

<main id="content" role="main">
                <%-- Location bar --%>
<%
    if (locbar)
    {
%>
<div class="container">
	<br>
                <dspace:include page="/layout/location-bar.jsp" />
</div>                
<%
    }
%>


        <%-- Page contents --%>
<div class="container fullheight">
<% if (request.getAttribute("dspace.layout.sidebar") != null) { %>
	<div class="row">
		<div class="col-md-9">
<% } %>		