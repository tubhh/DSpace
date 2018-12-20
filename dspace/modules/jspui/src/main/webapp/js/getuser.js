// URN registrieren fuer jspui (in layout/footer-submission.jsp)

/*
var dspaceid = document.getElementsByName("workspace_item_id")[0].value;
*/

var emailelement = document.getElementById( "tuhh_uploader_email" );
var authorelement = document.getElementsByName( "dc_contributor_author_last" ).length;

if ((emailelement || authorelement > 0) && getUrlVars().length == 1) {

    var userhttp = null;
    if (window.XMLHttpRequest) {
        userhttp = new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        userhttp = new ActiveXObject("Microsoft.XMLHTTP");
    }

    if (userhttp != null) {
        userhttp.open( "GET" , "/dspace-scripts/getUserdata.php?JSESSION=" + JSESSION , true );

        userhttp.onreadystatechange = setuser;
        userhttp.send( null );
    }
}

function setuser() {
    if ( userhttp.readyState == 4 ) {
        var emailelement_f = document.getElementById( "tuhh_uploader_email" );
        var jsonResponse = userhttp.responseText;
        var data = JSON.parse(jsonResponse);
        var author_count = document.getElementsByName( "dc_contributor_author_last_1" ).length;

        if (author_count == 0 && authorelement > 0) {
            var lastnameelement = document.getElementsByName( "dc_contributor_author_last" )[0];
            var firstnameelement = document.getElementsByName( "dc_contributor_author_first" )[0];

            if (lastnameelement.value == "") {
                lastnameelement.value = data.lastname;
                firstnameelement.value = data.firstname;
            }
        }

        if (emailelement_f.value == "") {
            emailelement_f.value = data.email;
        }

    }
}

// Read a page's GET URL variables and return them as an associative array.
function getUrlVars()
{
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}