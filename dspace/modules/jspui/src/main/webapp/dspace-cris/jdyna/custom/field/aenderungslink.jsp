<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocaleSupport"%>

<c:if test="${!empty anagraficaObject.anagrafica4view['aenderungslink']}">
<div class="dynaClear">&nbsp;</div>
<fmt:message key="contactbd.editdata.link" />
<div class="dynaClear">&nbsp;</div>
</c:if>