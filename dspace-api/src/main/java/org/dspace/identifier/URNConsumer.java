/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.identifier;

import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.dspace.identifier.DOIIdentifierProvider;
import org.dspace.identifier.IdentifierException;
import org.dspace.identifier.IdentifierNotFoundException;
import org.dspace.search.SearchConsumer;
import org.apache.commons.lang.StringUtils;
import org.dspace.utils.DSpace;
import org.dspace.workflow.WorkflowItem;

/**
 *
 * @author Oliver Goldschmidt (o dot goldschmidt at tu hyphen hamburg dot de)
 */
public class URNConsumer implements Consumer
{
    /** log4j logger */
    private static Logger log = Logger.getLogger(URNConsumer.class);

    static final String CFG_PREFIX = "identifier.urn.prefix";

    // Metadata field name elements
    // TODO: move these to MetadataSchema or some such?
    public static final String MD_SCHEMA = "dc";
    public static final String DOI_ELEMENT = "identifier";
    public static final String DOI_QUALIFIER = "uri";

    protected static String getPrefix()
    {
        String prefix = (new DSpace()).getSingletonService(ConfigurationService.class).getProperty(CFG_PREFIX);
        if (StringUtils.isBlank(prefix))
        {
            log.warn("Cannot find DOI prefix in configuration!");
            throw new RuntimeException("Unable to load URN prefix from "
                    + "configuration. Cannot find property " +
                    CFG_PREFIX + ".");
        }
        return prefix;
    }

    protected static String getCollectionException()
    {
        String ignorecollection = (new DSpace()).getSingletonService(ConfigurationService.class).getProperty("identifier.urn.ignorecollection");
        return ignorecollection;
    }

    @Override
    public void initialize() throws Exception {
        // nothing to do
        // we can ask spring to give as a properly setuped instance of
        // DOIIdentifierProvider. Doing so we don't have to configure it and
        // can load it in consume method as this is not very expensive.
        
    }
    
    // as we use asynchronous metadata update, our updates are not very expensive.
    // so we can do everything in the consume method.
    @Override
    public void consume(Context ctx, Event event) throws Exception {
        if (event.getSubjectType() != Constants.ITEM)
        {
            log.warn("URNConsumer should not have been given this kind of "
                    + "subject in an event, skipping: " + event.toString());
            return;
        }

        DSpaceObject dso = event.getSubject(ctx);
        //FIXME
        if (!(dso instanceof Item))
        {
            log.warn("URNConsumer got an event whose subject was not an item, "
                    + "skipping: " + event.toString());
            return;
        }
        Item item = (Item) dso;

        Metadatum[] urnFields = item.getMetadata("tuhh", "identifier", "urn", Item.ANY);
        if (urnFields.length == 0) {

            Collection col = item.getOwningCollection();
            if (col == null)
            {
                // check if we have a workspace item, they store the collection separately.
                WorkspaceItem wsi = WorkspaceItem.findByItem(ctx, item);
                if (wsi != null)
                {
                    col = wsi.getCollection();
                }
                if (col == null)
                {
                    // same for the workflow item
                    WorkflowItem wfi = WorkflowItem.findByItem(ctx, item);
                    if (wfi != null)
                    {
                        col = wfi.getCollection();
                    }
                }
            }
            String ch = col.getHandle();
            String ignorecoll = getCollectionException();

            log.debug("Collection Handle is "+ch+". Will ignore "+ignorecoll+"\n");

            if (!ch.equals(ignorecoll)) {
                log.info("Calculating and storing URN for item "+item.getID()+" to tuhh.identifier.urn\n");
                String urn = null;

                String value = getPrefix()+item.getID();
                char cs = this.calculateChecksum(value);
                urn = value + cs;
                item.addMetadata("tuhh", "identifier", "urn", null, urn, null, -1);
                item.updateMetadata();
                item.update();
                ctx.getDBConnection().commit();
            }
        }
    }

    @Override
    public void end(Context ctx) throws Exception {


    }

    @Override
    public void finish(Context ctx) throws Exception {
        // nothing to do
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
        log.debug("URN " + urn + " wurde zu " + NBNK + " mit Laenge " + Integer.toString(z));
        char[] URN = NBNK.toCharArray();
        int pos = 1;
        int sum = 0;
        for (char l: URN) {
        log.debug("Berechne " + Integer.toString(sum) + "+(" + l + "*" + Integer.toString(pos) + ")");
            sum = sum+(Character.getNumericValue(l)*pos);
            pos++;
        log.debug("Summe ist " + Integer.toString(sum));
        }
        int lz = Character.getNumericValue(NBNK.charAt(NBNK.length() - 1));
        int quot = (int) java.lang.Math.floor(sum/lz);
        String quots = Integer.toString(quot);
        log.debug("Quotient ist " + quot);
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
