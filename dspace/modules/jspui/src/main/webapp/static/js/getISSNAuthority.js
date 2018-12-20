// ISSN des Journals automatisch einsetzen

/*
var dspaceid = document.getElementsByName("workspace_item_id")[0].value;
*/
if (document.getElementsByName("page").length > 0) {
    var page = document.getElementsByName("page")[0].value;
    var journal = document.getElementById("dc_relation_ispartof");
    var issn = document.getElementById( "dc_identifier_issn" );
    var seriesSelect = document.getElementsByName("dc_relation_ispartof")[0];
    var series = document.getElementsByName( "dc_relation_ispartofseries_series_1" )[0];
    if (issn != null) {
        var httpissn = null;
/*
    if (issn.value == "") {
        check_issn();
    }
*/
        if (journal != null) {
            journal.onchange = check_issn;
            journal.onblur = check_issn;
        }
        issn.onchange = check_jtitle;
        issn.onblur = check_jtitle;
        issn.onkeyup = check_jtitle;
        issn.onkeydown = check_jtitle;
        issn.oncut = check_jtitle;
        issn.onpaste = check_jtitle;
    }
    seriesSelect.onchange = input_series;
}


function input_series() {
    series.value = seriesSelect.value;
}

function check_issn() {
    if (window.XMLHttpRequest) {
        httpissn = new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        httpissn = new ActiveXObject("Microsoft.XMLHTTP");
    }

    if (httpissn != null && issn.value == "") {
        httpissn.open( "GET" , "/dspace-scripts/getISSNSuggestions.php?name=" +  journal.value, true );

        httpissn.onreadystatechange = setissn;
        httpissn.send( null );
    }
}

function check_jtitle() {
    if (window.XMLHttpRequest) {
        httpjtitle = new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        httpjtitle = new ActiveXObject("Microsoft.XMLHTTP");
    }

    if (httpjtitle != null && journal.value == "") {
        httpjtitle.open( "GET" , "/dspace-scripts/getISSNSuggestions.php?issn=" +  issn.value, true );

        httpjtitle.onreadystatechange = setjtitle;
        httpjtitle.send( null );
    }
}

function setissn() {
    if ( httpissn.readyState == 4 ) {
        var jsonResponse = httpissn.responseText;
        var data = JSON.parse(jsonResponse);
        if (issn.value == "") {
            issn.value = data[0];
        }
    }
}

function setjtitle() {
    if ( httpjtitle.readyState == 4 ) {
        var jsonResponse = httpjtitle.responseText;
        var data = JSON.parse(jsonResponse);
        if (journal.value == "") {
            journal.value = data;
        }
    }
}
