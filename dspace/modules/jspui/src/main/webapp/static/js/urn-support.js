(function($){
// URN registrieren fuer jspui (in layout/footer-submission.jsp)
if ((document.getElementById( "tuhh_identifier_urn" ) !== null) && ((document.getElementsByName("workspace_item_id") !== null && document.getElementsByName("workspace_item_id").length > 0) || (document.getElementsByName("workflow_id") !== null && document.getElementsByName("workflow_id").length > 0))) {
    if (document.getElementsByName("workspace_item_id") !== null && document.getElementsByName("workspace_item_id").length > 0) {
        var dspaceid = document.getElementsByName("workspace_item_id")[0].value;

    //var page = document.getElementsByName("page")[0].value;

        var edhttp = null;
        if (window.XMLHttpRequest) {
            edhttp = new XMLHttpRequest();
        } else if (window.ActiveXObject) {
            edhttp = new ActiveXObject("Microsoft.XMLHTTP");
        }

        if (edhttp != null) {
            edhttp.open( "GET" , "/dspace-scripts/urn.php?dspaceid=" + dspaceid , true );

            edhttp.onreadystatechange = edrecommend;
            edhttp.send( null );
        }
    }
    document.getElementById( "tuhh_identifier_urn" ).readOnly = true;
}

function edrecommend() {
    if ( edhttp.readyState == 4 ) {
        var htmlResponse = edhttp.responseText;
        if (document.getElementById( "tuhh_identifier_urn" ).value == '') {
            document.getElementById( "tuhh_identifier_urn" ).value = htmlResponse;
        }
    var urn = htmlResponse;
    $(".help-block:contains('You can call the URN by appending it to the URL http://nbn-resolving.de/')").html(function () {
        return $(".help-block:contains('You can call the URN by appending it to the URL http://nbn-resolving.de/')").html().replace('You can call the URN by appending it to the URL http://nbn-resolving.de/. This document will get the following URN:', 'You can call the URN once it has been registered with the URL http://nbn-resolving.de/'+urn+'.');
    });
    $(".help-block:contains('URN aufrufen, indem Sie ihn an die URL http://nbn-resolving.de/ ')").html(function () {
        return $(".help-block:contains('URN aufrufen, indem Sie ihn an die URL http://nbn-resolving.de/')").html().replace('Sie können den URN aufrufen, indem Sie ihn an die URL http://nbn-resolving.de/ anhängen. Diesem Dokument wird der folgende URN zugewiesen:', 'Sie können den URN nach der Registrierung über die URL http://nbn-resolving.de/'+urn+' aufrufen.');
    });
    }
}
})(jQuery);
