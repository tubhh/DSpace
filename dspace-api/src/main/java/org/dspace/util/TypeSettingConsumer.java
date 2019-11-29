/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.util;

import org.apache.log4j.Logger;
import org.dspace.content.Metadatum;
import org.dspace.content.MetadataValue;
import org.dspace.content.Bitstream;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.apache.commons.lang.StringUtils;
import org.dspace.utils.DSpace;
import org.dspace.workflow.WorkflowItem;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.dspace.authorize.AuthorizeException;
import java.sql.SQLException;


/**
 * A basic link checker that is designed to be extended. By default this link checker
 * will check that all links stored in anyschema.anyelement.uri metadata fields return
 * a 20x status code.
 *
 * This link checker can be enhanced by extending this class, and overriding the
 * getURLs and checkURL methods.
 *
 * @author Oliver Goldschmidt
 */

public class TypeSettingConsumer implements Consumer
{

    // The log4j logger for this class
    private static Logger log = Logger.getLogger(TypeSettingConsumer.class);


    @Override
    public void initialize() throws Exception {
        // nothing to do
    }

    // as we use asynchronous metadata update, our updates are not very expensive.
    // so we can do everything in the consume method.
    @Override
    public void consume(Context ctx, Event event) throws Exception {
        if (event.getSubjectType() != Constants.ITEM)
        {
            log.warn("TypeSetter should not have been given this kind of "
                    + "subject in an event, skipping: " + event.toString());
            return;
        }

        DSpaceObject dso = event.getSubject(ctx);
        //MetadataValue.PARENT_PLACEHOLDER_VALUE

        // Unless this is  an item, we'll skip this item
        if (dso.getType() == Constants.ITEM)
        {
            Item item = (Item)dso;
            //Metadatum[] casrai = item.getMetadata("dc", "type", "casrai", Item.ANY);
            //Metadatum[] dcmitype = item.getMetadata("dcterms", "DCMIType", Item.ANY, Item.ANY);
            Metadatum[] types = item.getMetadata("dc", "type", null, Item.ANY);
            if (types.length > 0) {
                String type = types[0].value;
                Metadatum[] thesistypes = item.getMetadata("dc", "type", "thesis", Item.ANY);
                if (type != "Thesis" || (type == "Thesis" && thesistypes.length > 0)) {
                    try {
                        String thesistype = "";
                        if (thesistypes.length > 0) {
                            thesistype = thesistypes[0].value;
                        }

                        item.clearMetadata("dc", "type", "driver", Item.ANY);
                        item.clearMetadata("dc", "type", "dini", Item.ANY);
                        item.clearMetadata("dc", "type", "casrai", Item.ANY);
                        item.clearMetadata("tuhh", "type", "opus", Item.ANY);
                        item.clearMetadata("dcterms", "DCMIType", Item.ANY, Item.ANY);

                        Map<String, String> typeset = mapTypeArray(type, thesistype);
                        item.addMetadata("dc", "type", "dini", null, typeset.get("dini"), null, -1);
                        item.addMetadata("dc", "type", "driver", null, typeset.get("driver"), null, -1);
                        item.addMetadata("dc", "type", "casrai", null, typeset.get("casrai"), null, -1);
                        item.addMetadata("dcterms", "DCMIType", null, null, typeset.get("dcmi"), null, -1);
                        item.addMetadata("tuhh", "type", "opus", null, typeset.get("opus"), null, -1);

                        item.updateMetadata();
                        item.update();
                        ctx.getDBConnection().commit();
                    } catch (AuthorizeException ae) {
                        // Something went wrong
                        logDebugMessage(ae.getMessage());
                    } catch (SQLException sqle) {
                        // Something went wrong
                        logDebugMessage(sqle.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Debugging logging if required
     *
     * @param message The message to log
     */
    private void logDebugMessage(String message)
    {
        if (log.isDebugEnabled())
        {
            log.debug(message);
        }
    }

    /**
     * Internal utitity method to get a description of the handle
     *
     * @param item The item to get a description of
     * @return The handle, or in workflow
     */
    private static String getItemHandle(Item item)
    {
        String handle = item.getHandle();
        return (handle != null) ? handle: " in workflow";
    }

    private static Map<String, String> mapTypeArray(String mainType, String thesisType) {
        Map<String, String> ret = new HashMap<String, String>();
        switch (mainType) {
            case "Thesis":
                switch (thesisType) {
                    case "bachelorThesis":
                        ret.put("opus", "Bachelor Thesis");
                        ret.put("dini", "bachelorThesis");
                        ret.put("driver", "bachelorThesis");
                        ret.put("casrai", "Supervised Student Publication");
                        ret.put("dcmi", "Text");
                        break;
                    case "diplomaThesis":
                        ret.put("opus", "Diplomarbeit, Magisterarbeit");
                        ret.put("dini", "masterThesis");
                        ret.put("driver", "masterThesis");
                        ret.put("casrai", "Supervised Student Publication");
                        ret.put("dcmi", "Text");
                        break;
                    case "magisterThesis":
                        ret.put("opus", "Diplomarbeit, Magisterarbeit");
                        ret.put("dini", "masterThesis");
                        ret.put("driver", "masterThesis");
                        ret.put("casrai", "Supervised Student Publication");
                        ret.put("dcmi", "Text");
                        break;
                    case "doctoralThesis":
                        ret.put("opus", "Dissertation");
                        ret.put("dini", "doctoralThesis");
                        ret.put("driver", "doctoralThesis");
                        ret.put("casrai", "Dissertation");
                        ret.put("dcmi", "Text");
                        break;
                    case "habilitation":
                    case "habilitationThesis":
                        ret.put("opus", "Habilitation");
                        ret.put("dini", "doctoralThesis");
                        ret.put("driver", "doctoralThesis");
                        ret.put("casrai", "Dissertation");
                        ret.put("dcmi", "Text");
                        break;
                    case "masterThesis":
                        ret.put("opus", "Masterarbeit");
                        ret.put("dini", "masterThesis");
                        ret.put("driver", "masterThesis");
                        ret.put("casrai", "Supervised Student Publication");
                        ret.put("dcmi", "Text");
                        break;
                    default:
                        ret.put("opus", "Andere Abschlussarbeit");
                        ret.put("dini", "StudyThesis");
                        ret.put("driver", "report");
                        ret.put("casrai", "Supervised Student Publication");
                        ret.put("dcmi", "Text");
                }
                break;
            case "manual":
                ret.put("opus", "Anleitung (Manual)");
                ret.put("dini", "report");
                ret.put("driver", "report");
                ret.put("casrai", "Manual");
                ret.put("dcmi", "Text");
                break;
            case "workingPaper":
            case "Working Paper":
            ret.put("opus", "ResearchPaper");
            ret.put("dini", "workingPaper");
            ret.put("driver", "workingPaper");
            ret.put("casrai", "Working Paper");
            ret.put("dcmi", "Text");
            break;
        case "article":
        case "Article":
            ret.put("opus", "(wissenschaftlicher) Artikel");
            ret.put("dini", "article");
            ret.put("driver", "article");
            ret.put("casrai", "Journal Article");
            ret.put("dcmi", "Text");
            break;
        case "Image":
            ret.put("opus", "Bild");
            ret.put("dini", "Image");
            ret.put("driver", "other");
            ret.put("casrai", "Other");
            ret.put("dcmi", "Image");
            break;
        case "Image, 3-D":
            ret.put("opus", "3D Bild");
            ret.put("dini", "Image");
            ret.put("driver", "other");
            ret.put("casrai", "Other");
            ret.put("dcmi", "Image");
            break;
        case "book":
            ret.put("opus", "Buch (Monographie)");
            ret.put("dini", "book");
            ret.put("driver", "book");
            ret.put("casrai", "Book");
            ret.put("dcmi", "Text");
            break;
        case "festschrift":
        case "Festschrift":
            ret.put("opus", "Festschrift");
            ret.put("dini", "book");
            ret.put("driver", "book");
            ret.put("casrai", "Book");
            ret.put("dcmi", "Text");
            break;
        case "Poster":
            ret.put("opus", "Poster");
            ret.put("dini", "Other");
            ret.put("driver", "other");
            ret.put("casrai", "Conference Poster");
            ret.put("dcmi", "Image");
            break;
        case "bookPart":
        case "inBook":
            ret.put("opus", "InBuch (Kapitel / Teil einer Monographie)");
            ret.put("dini", "bookPart");
            ret.put("driver", "bookPart");
            ret.put("casrai", "Book Chapter");
            ret.put("dcmi", "Text");
            break;
        case "inProceedings":
            ret.put("opus", "InProceedings (Aufsatz / Paper einer Konferenz etc.)");
            ret.put("dini", "contributionToPeriodical");
            ret.put("driver", "contributionToPeriodical");
            ret.put("dcmi", "Text");
            ret.put("casrai", "Conference Paper");
            break;
        case "Map":
            ret.put("opus", "Kartenmaterial");
            ret.put("dini", "CartographicalMaterial");
            ret.put("driver", "other");
            ret.put("casrai", "Other");
            ret.put("dcmi", "Image");
            break;
        case "Learning Object":
            ret.put("opus", "Lernmaterial");
            ret.put("dini", "CourseMaterial");
            ret.put("driver", "other");
            ret.put("casrai", "Other");
            ret.put("dcmi", "InteractiveResource");
            break;
        case "Presentation":
            ret.put("opus", "Präsentation");
            ret.put("dini", "Other");
            ret.put("driver", "other");
            ret.put("casrai", "Other");
            ret.put("dcmi", "InteractiveResource");
            break;
        case "Software":
            ret.put("opus", "Software");
            ret.put("dini", "Software");
            ret.put("driver", "other");
            ret.put("casrai", "Other");
            ret.put("dcmi", "Software");
            break;
        case "Technical Report":
        case "report":
            ret.put("opus", "Report (Bericht)");
            ret.put("dini", "report");
            ret.put("driver", "report");
            ret.put("casrai", "Report");
            ret.put("dcmi", "Text");
            break;
        case "Video":
            ret.put("opus", "Video");
            ret.put("dini", "MovingImage");
            ret.put("driver", "other");
            ret.put("casrai", "Other");
            ret.put("dcmi", "Image");
            break;
        case "preprint":
        case "Preprint":
            ret.put("opus", "Preprint (Vorabdruck)");
            ret.put("dini", "preprint");
            ret.put("driver", "preprint");
            ret.put("casrai", "Other");
            ret.put("dcmi", "Text");
            break;
        case "Journal":
        case "Journal Issue":
        case "journal":
            ret.put("opus", "Journal (Komplette Ausgabe eines Zeitschriftenheftes)");
            ret.put("dini", "PeriodicalPart");
            ret.put("driver", "other");
            ret.put("casrai", "Journal Issue");
            ret.put("dcmi", "Text");
            break;
        case "lecture":
        case "Lecture":
            ret.put("opus", "Vorlesung");
            ret.put("dini", "lecture");
            ret.put("driver", "lecture");
            ret.put("casrai", "Other");
            ret.put("dcmi", "Text");
            break;
        case "StudyThesis":
        case "Study Thesis":
            ret.put("opus", "Studienarbeit");
            ret.put("dini", "StudyThesis");
            ret.put("driver", "other");
            ret.put("casrai", "Other");
            ret.put("dcmi", "Text");
            break;
        case "Proceedings":
            ret.put("opus", "Proceedings (Komplette Ausgabe einer Konferenz etc.)");
            ret.put("dini", "conferenceObject");
            ret.put("driver", "conferenceObject");
            ret.put("casrai", "Book");
            ret.put("dcmi", "Text");
            break;
        case "Music":
        case "Sound":
            ret.put("opus", "Musik");
            ret.put("dini", "Sound");
            ret.put("driver", "Sound");
            ret.put("casrai", "Other");
            ret.put("dcmi", "Sound");
            break;
        case "Dataset":
            ret.put("opus", "Dataset");
            ret.put("dini", "ResearchData");
            ret.put("driver", "other");
            ret.put("casrai", "Other");
            ret.put("dcmi", "Dataset");
            break;
        case "Audiovisual":
            ret.put("opus", "Audiovisuell");
            ret.put("dini", "MovingImage");
            ret.put("driver", "other");
            ret.put("casrai", "Online Resource");
            ret.put("dcmi", "Image");
            break;
        case "DataPaper":
            ret.put("opus", "DataPaper");
            ret.put("dini", "ResearchData");
            ret.put("driver", "other");
            ret.put("casrai", "Online Resource");
            ret.put("dcmi", "Text");
            break;
        case "InteractiveResource":
            ret.put("opus", "Interactive Resource");
            ret.put("dini", "Other");
            ret.put("driver", "other");
            ret.put("casrai", "Online Resource");
            ret.put("dcmi", "InteractiveResource");
            break;
        case "Other":
            ret.put("opus", "Sonstiges");
            ret.put("dini", "Other");
            ret.put("driver", "other");
            ret.put("casrai", "Other");
            ret.put("dcmi", "Text");
            break;
        case "Text":
            ret.put("opus", "Text");
            ret.put("dini", "ResearchData");
            ret.put("driver", "other");
            ret.put("casrai", "Online Resource");
            ret.put("dcmi", "Text");
            break;
        case "article-review":
            //Cerif: Journal Article Review
            ret.put("opus", "Review (Artikel)");
            ret.put("dini", "review");
            ret.put("driver", "review");
            ret.put("casrai", "Journal Article");
            ret.put("dcmi", "Text");
            break;
        case "letter":
            //Cerif: Letter to Editor
            ret.put("opus", "Letter (to Editor)");
            ret.put("dini", "Other");
            ret.put("driver", "other");
            ret.put("casrai", "Other");
            ret.put("dcmi", "Text");
            break;
        case "commentary":
            //Cerif:Commentary
            ret.put("opus", "Kommentar");
            ret.put("dini", "Other");
            ret.put("driver", "other");
            ret.put("casrai", "Other");
            ret.put("dcmi", "Text");
            break;
        default:
            ret.put("opus", mainType);
            ret.put("dini", "Other");
            ret.put("driver", "other");
            ret.put("casrai", "Other");
            ret.put("dcmi", "Text");
            break;
    }
    return ret;
    }

    @Override
    public void end(Context ctx) throws Exception {
    }

    @Override
    public void finish(Context ctx) throws Exception {
        // nothing to do
    }

}
