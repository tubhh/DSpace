<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:if test="${!empty anagraficaObject.anagrafica4view['orcid']}">
<div class="dynaClear">&nbsp;</div>
<div class="dynaField" style="min-width:5em;">
<span class="dynaLabel" style="width:15em;">ORCID</span>

<div id="orcidDiv" class="dynaFieldValue">
<c:choose>
    <c:when test="${!empty anagraficaObject.anagrafica4view['system-orcid-token-authenticate']}">
        <span style="min-width: 30em;"><a target="_blank" href="https://orcid.org/${anagraficaObject.anagrafica4view['orcid'][0]}"><img src="https://orcid.org/sites/default/files/images/orcid_16x16.png" /> orcid.org/${anagraficaObject.anagrafica4view['orcid'][0]}</a></span>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${!empty anagraficaObject.anagrafica4view['orcid-authorized']}">
                <span style="min-width: 30em;"><a target="_blank" href="https://orcid.org/${anagraficaObject.anagrafica4view['orcid'][0]}"><img src="https://orcid.org/sites/default/files/images/orcid_16x16.png" /> orcid.org/${anagraficaObject.anagrafica4view['orcid'][0]}</a> (authentifiziert via Kontaktdatenbank)</span>
            </c:when>
            <c:otherwise>
                <span style="min-width: 30em;"><a target="_blank" href="https://orcid.org/${anagraficaObject.anagrafica4view['orcid'][0]}"> orcid.org/${anagraficaObject.anagrafica4view['orcid'][0]}</a></span>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>
</div>
</div>
<div class="dynaClear">&nbsp;</div>
</c:if>