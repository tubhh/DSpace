<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:set var="entity" value="${researcher}" scope="request" />

<c:if test="${anagraficaObject.anagrafica4view['staffStatus'][0]=='staff'}">

<div class="dynaClear">&nbsp;</div>
<div class="dynaField" style="min-width:5em;">
<span class="dynaLabel" style="width:15em;"><fmt:message key="org.dspace.cris.contact-uid.label" /></span>

<div id="contactDiv" class="dynaFieldValue">
    <a href="https://kontakt.tuhh.de/suchergebnis.php?id=${anagraficaObject.anagrafica4view['contact-uid'][0]}">https://kontakt.tuhh.de/suchergebnis.php?id=${anagraficaObject.anagrafica4view['contact-uid'][0]}</a>
</div>
<div class="dynaClear">&nbsp;</div>

</c:if>