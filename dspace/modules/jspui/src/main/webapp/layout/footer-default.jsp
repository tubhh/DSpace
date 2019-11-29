<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>
<%--
  - Footer for home page
  --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ page contentType="text/html;charset=UTF-8" %>

<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.dspace.app.webui.util.UIUtil" %>
<%@ page import="org.dspace.core.NewsManager" %>
<%@ page import="java.util.Locale"%>
<%@ page import="javax.servlet.jsp.jstl.core.*" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.LocaleSupport" %>
<%@ page import="org.dspace.core.I18nUtil" %>
<%@ page import="org.dspace.app.webui.util.UIUtil" %>
<%@ page import="org.dspace.core.ConfigurationManager" %>

<script type="text/javascript" src="<%= request.getContextPath() %>/static/js/jquery_manipulation-default.js"></script>

<%
    String sidebar = (String) request.getAttribute("dspace.layout.sidebar");
%>

            <%-- Right-hand side bar if appropriate --%>
<%
    if (sidebar != null)
    {
%>
	</div>
	<div class="col-md-3">
                    <%= sidebar %>
    </div>
    </div>       
<%
    }
%>
</div>
</main>
            <%-- Page footer --%>
             <footer class="navbar navbar-inverse navbar-bottom">
<%
    Locale sessionLocale = UIUtil.getSessionLocale(request);
    Config.set(request.getSession(), Config.FMT_LOCALE, sessionLocale);
    String iconsbar = NewsManager.readNewsFile(LocaleSupport.getLocalizedMessage(pageContext, "iconsbar.html"));
%>
    <div class="navbar navbar-bottom">
        <%= iconsbar %>
    </div>
             <div id="designedby" class="container text-muted"><fmt:message key="jsp.layout.navbar-default.tubdok-impress"/>&nbsp;-
                                <a target="_blank" href="<%= request.getContextPath() %>/feedback"><fmt:message key="jsp.layout.footer-default.feedback"/></a>&nbsp;-
                                <fmt:message key="jsp.layout.navbar-default.tubdok-contact"/>&nbsp;-
                                <fmt:message key="jsp.layout.navbar-default.data-privacy"/>

			<div id="footer_feedback" class="pull-right">                                    
                                <p class="text-muted"><fmt:message key="jsp.layout.footer-default.text"/>&nbsp;-
             <fmt:message key="jsp.layout.footer-default.version-by"/> <a href="http://www.4science.it/en/dspace-cris/">
             <img src="<%= request.getContextPath() %>/image/logo-4science-small.png"
                                    alt="Logo 4SCIENCE" height="32px"/></a>
                                    - powered by <img src="<%= request.getContextPath() %>/image/tub_myriad_40h.png"
                                    alt="TUHH University Library" height="32px"/>
                                </p>
                                </div>
			</div>

    </footer>
    </body>
</html>