<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@page import="javax.servlet.jsp.jstl.fmt.LocaleSupport"%>

<div class="dynaClear">&nbsp;</div>
<div class="dynaField" style="min-width:5em;">
<span class="dynaLabel" style="width:15em;"><fmt:message key="RPAdditionalFieldStorage.staffStatus.label" /></span>

<div id="statusDiv" class="dynaFieldValue">
<fmt:message key="ItemCrisRefDisplayStrategy.rp.${anagraficaObject.anagrafica4view['staffStatus'][0]}.title" />
</div>
</div>
<div class="dynaClear">&nbsp;</div>
