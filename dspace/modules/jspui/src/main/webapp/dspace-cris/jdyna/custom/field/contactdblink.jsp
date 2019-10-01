<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocaleSupport"%>

<c:if test="${researcher.sourceRef=='LDAP'}">

		    <div class="panel-group col-md-12" id="contactdblink">
				    <div class="panel panel-default">
					    <div class="panel-heading">
    						<h4 class="panel-title">
    						    <a data-toggle="collapse" data-parent="#contactdblink" href="#collapseOnecontactdblink">
        						    <fmt:message key="ResearcherPage.box.personaldataedit.label" />
    						    </a>
    						</h4>
					    </div>
					    <div id="collapseOnecontactdblink" class="panel-collapse collapse in">
						<div class="panel-body">

<div class="dynaClear">&nbsp;</div>
<fmt:message key="contactbd.editdata.link" />
<div class="dynaClear">&nbsp;</div>

					        </div>
					  </div>
				   </div>
			    </div>
</c:if>