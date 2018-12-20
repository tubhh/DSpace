// GND-Funktionalität für normierte Schlagwörter bei Meldeformular fuer jspui (in layout/footer-submission.jsp)
function showOgnd() {window.open("http://"+window.location.host+"/dspace-scripts/ognd.php","OGND","height=700,width=540,status=no,menubar=no,scrollbars=yes");}

var xx = document.querySelectorAll("div.help-block");

for (var i = 0; i < xx.length; i++) {
    if (xx[i].innerHTML.search('Waehlen Sie mindestens ein Schlagwort aus der Gemeinsamen Normdatei aus.') != -1) {
        xx[i].innerHTML = '<a href="javascript:showOgnd()">Klicken Sie hier um Schlagworte aus der Gemeinsamen Normdatei auszuwählen.</a>';
    }
}
var yy = document.getElementById("dc_subject_gnd");
if (yy != null) {
    yy.readOnly = true;
}


// JQuery-Loesung
//(function($){
//    $("div:contains('Fundref')").html(function () {
//        return $(this).html().replace('Fundref', '<a href="http://fundref.org/fundref/fundref_registry.html">Fundref</a>'); 
//    });
    //$("div:contains('Bitte geben Sie hier die DOI der Sponsoren an (aus Fundref).')").html('Bitte geben Sie hier die DOI der Sponsoren an (aus <a href="http://fundref.org/fundref/fundref_registry.html">Fundref</a>).');
//})(jQuery);
