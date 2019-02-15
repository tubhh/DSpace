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
             <div id="designedby" class="container text-muted">
                                <a target="_blank" href="<%= request.getContextPath() %>/feedback"><fmt:message key="jsp.layout.footer-default.feedback"/></a>&nbsp;-
                                <fmt:message key="jsp.layout.navbar-default.tubdok-contact"/>

			<div id="footer_feedback" class="pull-right">                                    
                                <p class="text-muted"><fmt:message key="jsp.layout.footer-default.text"/>&nbsp;-
             <fmt:message key="jsp.layout.footer-default.version-by"/> <a href="http://www.4science.it/en/dspace-and-dspace-cris-services/">
             <img src="<%= request.getContextPath() %>/image/logo-4science-small.png"
                                    alt="Logo 4SCIENCE" height="32px"/></a>
                                    - powered by <img src="<%= request.getContextPath() %>/image/tub_myriad_40h.png"
                                    alt="TUHH University Library" height="32px"/>
                                </p>
                                </div>
			</div>

    </footer>
<script type="text/javascript">
  var _paq = _paq || [];
  /* tracker methods like "setCustomDimension" should be called before "trackPageView" */
  _paq.push(['trackPageView']);
  _paq.push(['enableLinkTracking']);
  (function() {
    var u="//www.tub.tuhh.de/ext/piwik/";
    _paq.push(['setTrackerUrl', u+'piwik.php']);
    _paq.push(['setSiteId', '14']);
    var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0];
    g.type='text/javascript'; g.async=true; g.defer=true; g.src=u+'piwik.js'; s.parentNode.insertBefore(g,s);
  })();
</script>
<img src="https://www.tub.tuhh.de/ext/piwik/piwik.php?idsite=14&amp;rec=1" style="border:0" alt="" />
    </body>
</html>