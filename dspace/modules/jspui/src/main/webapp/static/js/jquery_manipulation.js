(function($){
    var xx = document.getElementsByName("dc_rights_ccversion");
    if (xx[0]) {
        xx[0].parentNode.parentNode.hide();
        xx[0].parentNode.parentNode.previousElementSibling.hide();
        var yy = document.getElementsByName("dc_rights_cc");
        if (yy[0].value != "cc-null" && yy[0].value != "" && yy[0].value != "pd") {
            xx[0].parentNode.parentNode.previousElementSibling.show();
            xx[0].parentNode.parentNode.show();
        }
        yy[0].onchange = function () {
            if (yy[0].value != "cc-null" && yy[0].value != "" && yy[0].value != "pd") {
                xx[0].parentNode.parentNode.previousElementSibling.show();
                xx[0].parentNode.parentNode.show();
            }
            else {
                xx[0].parentNode.parentNode.previousElementSibling.hide();
                xx[0].parentNode.parentNode.hide();
            }
        }
    }
/*
    $("div:contains('Fundref')").html(function () {
        return $(this).html().replace('Fundref', '<a href="http://fundref.org/fundref/fundref_registry.html">Fundref</a>');
    });
*/
})(jQuery);

/*
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
*/

/*
jQuery(function($){
    var replaced = $("body").html().replace(/Fundref/g,'<a href="http://fundref.org/fundref/fundref_registry.html">Fundref</a>');
    $("body").html(replaced);
});
*/
// ACHTUNG: nach Aufruf dieser Funktion wird kein JavaScript mehr ausgeführt! Es kommt zu einem Fehler, der saemtliche weitere Ausfuehrung verhindert