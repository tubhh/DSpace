(function($){
    $('td[headers="t1"]').addClass("t1");
/*
    $("div:contains('Reichen Sie uns diesen Nachweis bitte ein')").html(function () {
        return $(this).html().replace('Reichen Sie uns diesen Nachweis bitte ein', 'Reichen Sie uns diesen <a href="https://dspace.tub.tuhh.de/static/dozentenbestaetigung_tubdok_2015.pdf">Nachweis</a> bitte ein');
    });
    $("div:contains('Please submit this confirmation to us')").html(function () {
        return $(this).html().replace('Please submit this confirmation to us', 'Please submit this <a href="https://dspace.tub.tuhh.de/static/dozentenbestaetigung_tubdok_2015.pdf">confirmation</a> to us');
    });
    $("div:contains('Diplomarbeiten einen Nachweis der wissenschaftlichen Relevanz')").html(function () {
        return $(this).html().replace('Diplomarbeiten einen Nachweis der wissenschaftlichen Relevanz', 'Diplomarbeiten einen <a href="https://dspace.tub.tuhh.de/static/dozentenbestaetigung_tubdok_2015.pdf">Nachweis der wissenschaftlichen Relevanz</a>');
    });
    $("div:contains('you need a confirmation of scientific relevance for Bachelor')").html(function () {
        return $(this).html().replace('you need a confirmation of scientific relevance for Bachelor', 'you need a <a href="https://dspace.tub.tuhh.de/static/dozentenbestaetigung_tubdok_2015.pdf">confirmation of scientific relevance</a> for Bachelor');
    });
*/
})(jQuery);

var nl = document.getElementsByName("dc_rights_nationallicense")[0];
if (nl) {
    nl.onchange = input_text;
}

function input_text() {
    if (nl.value == "true") {
        document.getElementsByName("dc_rights_nltext")[0].value = "Dieser Beitrag ist mit Zustimmung des Rechteinhabers aufgrund einer (DFG geförderten) Allianz- bzw. Nationallizenz frei zugänglich. This publication is with permission of the rights owner freely accessible due to an Alliance licence and a national licence (funded by the DFG, German Research Foundation) respectively.";
    }
}
