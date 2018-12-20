// URN registrieren fuer jspui (in layout/footer-submission.jsp)
if (
  (document.getElementById( "tuhh_identifier_urn" ) !== null) && 
  (
    (document.getElementsByName("workspace_item_id") !== null && document.getElementsByName("workspace_item_id").length > 0) 
    || (document.getElementsByName("workflow_id") !== null && document.getElementsByName("workflow_id").length > 0)
  )) {
    var dspaceid;
    if (document.getElementsByName("workspace_item_id") !== null && document.getElementsByName("workspace_item_id").length > 0) {
        dspaceid = document.getElementsByName("workspace_item_id")[0].value;
    }
    else if (document.getElementsByName("workflow_id") !== null && document.getElementsByName("workflow_id").length > 0) {
        dspaceid = ".0"+document.getElementsByName("workflow_id")[0].value;
    }
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
    document.getElementById( "tuhh_identifier_urn" ).readOnly = true;
}

function edrecommend() {
    if ( edhttp.readyState == 4 ) {
        var htmlResponse = edhttp.responseText;
        if (document.getElementById( "tuhh_identifier_urn" ).value == '') {
            document.getElementById( "tuhh_identifier_urn" ).value = htmlResponse;
        }
    }
}
