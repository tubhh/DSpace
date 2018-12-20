/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.generator;

import org.apache.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.Context;
import java.lang.String;

public class UrnValueGenerator implements TemplateValueGenerator
{

    private static Logger log = Logger.getLogger(UrnValueGenerator.class);

    private String prefix;

    @Override
    public Metadatum[] generator(Context context, Item targetItem, Item templateItem,
            Metadatum metadatum, String extraParams)
    {

        Metadatum[] m = new Metadatum[1];
        m[0] = metadatum;
        String value = this.prefix+targetItem.getID();
        char cs = this.calculateChecksum(value);
        metadatum.value = value + cs;
        return m;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private char calculateChecksum(String urn) {
        urn.toLowerCase();
        char[] charArray = urn.toCharArray();
        String ca = "";
        for (char c: charArray) {
            switch (c) {
                case '9':
                    ca += "41";
                    break;
                case '8':
                    ca += "9";
                    break;
                case '7':
                    ca += "8";
                    break;
                case '6':
                    ca += "7";
                    break;
                case '5':
                    ca += "6";
                    break;
                case '4':
                    ca += "5";
                    break;
                case '3':
                    ca += "4";
                    break;
                case '2':
                    ca += "3";
                    break;
                case '1':
                    ca += "2";
                    break;
                case '0':
                    ca += "1";
                    break;
                case 'a':
                    ca += "18";
                    break;
                case 'b':
                    ca += "14";
                    break;
                case 'c':
                    ca += "19";
                    break;
                case 'd':
                    ca += "15";
                    break;
                case 'e':
                    ca += "16";
                    break;
                case 'f':
                    ca += "21";
                    break;
                case 'g':
                    ca += "22";
                    break;
                case 'h':
                    ca += "23";
                    break;
                case 'i':
                    ca += "24";
                    break;
                case 'j':
                    ca += "25";
                    break;
                case 'k':
                    ca += "42";
                    break;
                case 'l':
                    ca += "26";
                    break;
                case 'm':
                    ca += "27";
                    break;
                case 'n':
                    ca += "13";
                    break;
                case 'o':
                    ca += "28";
                    break;
                case 'p':
                    ca += "29";
                    break;
                case 'q':
                    ca += "31";
                    break;
                case 'r':
                    ca += "12";
                    break;
                case 's':
                    ca += "32";
                    break;
                case 't':
                    ca += "33";
                    break;
                case 'u':
                    ca += "11";
                    break;
                case 'v':
                    ca += "34";
                    break;
                case 'w':
                    ca += "35";
                    break;
                case 'x':
                    ca += "36";
                    break;
                case 'y':
                    ca += "37";
                    break;
                case 'z':
                    ca += "38";
                    break;
                case '-':
                    ca += "39";
                    break;
                case ':':
                    ca += "17";
                    break;
                case '_':
                    ca += "43";
                    break;
                case '.':
                    ca += "47";
                    break;
            }
        }
        String NBNK = ca;
        int z = NBNK.length();
        log.debug("URN " + urn + " wurde zu " + NBNK + "mit Laenge " + Integer.toString(z));
        char[] URN = NBNK.toCharArray();
        int pos = 1;
        int sum = 0;
        for (char l: URN) {
            sum = sum+(Character.getNumericValue(l)*pos);
            pos++;
        }
        int lz = Character.getNumericValue(NBNK.charAt(NBNK.length() - 1));
        int quot = (int) java.lang.Math.floor(sum/lz);
        String quots = Integer.toString(quot);
        //int laenge = quots.length();
        //char[] aqs = quots.toCharArray();
        //char pz = aqs[(laenge-1)];
        char pz = quots.charAt(quots.length() - 1);
        return pz;
/*
<------>$URN = preg_split("//", $NBNK);
<------>//foreach($URN as $key => $wert)
<------>//echo "$key => $wert <p>";
<------>//echo "NBN-Array: "."$URN<p>";
<------>for ($ii = 1;$ii <= count($URN);$ii++) {
<------><------>$sum = $sum+($URN[$ii]*$ii);
<------>}
//<---->echo "Produktsumme: "."$sum<p>";
<------>$lz = $URN[$z];
<------>//echo "letzte Zahl der konvertierten NBN: "."$lz<p>";
<------>$quot = floor($sum/$lz);
//<---->echo "Quotient: "."$quot<p>";
<------>$quots = (string)$quot;
<------>$laenge = strlen($quots);
<------>$aqs = preg_split("//", $quots);
<------>$pz = (int)$aqs[$laenge];

<------>$urn = $NBN . $pz;
<------>$resolveUrn = "http://nbn-resolving.de/".$urn;
*/
/*
<------>$sql_string = "INSERT INTO metadatavalue (item_id, metadata_field_id, text_value, text_lang, place) VALUES (".$dspace_id.",25,'".$resolveUrn."','de_DE','1')";
<------>pg_query($conn, $sql_string);
<------>$sql1_string = "INSERT INTO metadatavalue (item_id, metadata_field_id, text_value, text_lang, place) VALUES (".$dspace_id.",208,'".$urn."','de_DE','1')";
<------>pg_query($conn, $sql1_string);
<------>$sql2_string = "INSERT INTO metadatavalue (item_id, metadata_field_id, text_value, text_lang, place) VALUES (".$dspace_id.",210,'".$urn."','de_DE','1')";
<------>pg_query($conn, $sql2_string);
*/
/*
}

if ($urn) {
    print $urn;
}
*/
        //return 0;
    }
}
