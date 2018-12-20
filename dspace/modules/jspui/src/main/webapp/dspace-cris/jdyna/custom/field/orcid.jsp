<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:if test="${!empty anagraficaObject.anagrafica4view['orcid']}">
<div class="dynaClear">&nbsp;</div>
<div class="dynaField" style="min-width:5em;">
<span class="dynaLabel" style="width:10em;">ORCID</span>

<div id="orcidDiv" class="dynaFieldValue">
<c:choose>
    <c:when test="${empty anagraficaObject.anagrafica4view['system-orcid-token-authenticate']}">
        <span style="min-width: 30em;"><a target="_blank" href="https://sandbox.orcid.org/${anagraficaObject.anagrafica4view['orcid'][0]}"> sandbox.orcid.org/${anagraficaObject.anagrafica4view['orcid'][0]}</a></span>
    </c:when>
    <c:when test="${!empty anagraficaObject.anagrafica4view['system-orcid-token-authenticate']}">
        <span style="min-width: 30em;"><a target="_blank" href="https://sandbox.orcid.org/${anagraficaObject.anagrafica4view['orcid'][0]}"><img src="https://orcid.org/sites/default/files/images/orcid_16x16.png" /> sandbox.orcid.org/${anagraficaObject.anagrafica4view['orcid'][0]}</a></span>
    </c:when>
</c:choose>
</div>
</div>
<div class="dynaClear">&nbsp;</div>
</c:if>