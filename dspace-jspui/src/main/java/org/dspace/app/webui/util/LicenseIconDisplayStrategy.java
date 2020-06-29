/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.util.DCInput;
import org.dspace.app.util.DCInputSet;
import org.dspace.app.util.DCInputsReader;
import org.dspace.app.util.DCInputsReaderException;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.content.authority.Choice;
import org.dspace.content.authority.ChoiceAuthority;
import org.dspace.content.authority.Choices;
import org.dspace.core.Context;
import org.dspace.core.I18nUtil;
import org.dspace.core.PluginManager;
import org.dspace.core.Utils;

public class LicenseIconDisplayStrategy extends ASimpleDisplayStrategy
{

    private static final Logger log = Logger
            .getLogger(LicenseIconDisplayStrategy.class);

    private Map<String, DCInputsReader> valuePairMap = new HashMap<>();

    private void init() throws DCInputsReaderException
    {
log.debug("Initializing...");
        if(valuePairMap.isEmpty()) {
            for (Locale locale : I18nUtil.getSupportedLocales())
            {
log.debug("Reading Input file "+I18nUtil.getInputFormsFileName(locale)+" for language "+locale.getLanguage());
                valuePairMap.put(locale.getLanguage(),
                    new DCInputsReader(I18nUtil.getInputFormsFileName(locale)));
            }
        }
    }

    @Override
    public String getMetadataDisplay(HttpServletRequest hrq, int limit,
            boolean viewFull, String browseType, int colIdx, int itemid,
            String field, Metadatum[] metadataArray, boolean disableCrossLinks,
            boolean emph) throws JspException
    {
        try
        {
            init();
        }
        catch (DCInputsReaderException e)
        {
            log.error(e.getMessage(), e);
        }

        String resultString = "";
        try
        {
            Context obtainContext = UIUtil.obtainContext(hrq);
            Collection collection = Collection.find(obtainContext, colIdx);
            if (collection != null)
            {
                resultString = getResult(colIdx, field, metadataArray, resultString,
                        obtainContext, collection);
            }
            else
            {
                Item item = Item.find(obtainContext, itemid);
                collection = item.getParentObject();
                resultString = getResult(colIdx, field, metadataArray, resultString,
                        obtainContext, collection);
            }
log.debug("ResultString for DisplayStrategy is now "+resultString);
            // workaround, a sort of fuzzy match search in all valuepairs (possible wrong result due to the same stored value in many valuepairs)
/*
            if (StringUtils.isBlank(resultString))
            {
log.debug("ResultString is empty, checking it again...");
                String language = I18nUtil.getSupportedLocale(obtainContext.getCurrentLocale()).getLanguage();
                Map<String, List<String>> mappedValuePairs = valuePairMap.get(language)
                        .getMappedValuePairs();
                List<String> pairsnames = new ArrayList<String>();
                if (mappedValuePairs != null)
                {
                    for (String key : mappedValuePairs.keySet())
                    {
                        List<String> values = mappedValuePairs.get(key);
                        for (String vv : values)
                        {
                            if (StringUtils.equals(field, vv))
                            {
                                pairsnames.add(key);
                            }
                        }
                    }
                }

                for (String pairsname : pairsnames)
                {
                    ChoiceAuthority choice = (ChoiceAuthority) PluginManager
                            .getNamedPlugin(ChoiceAuthority.class, pairsname);

                    int ii = 0;
                    for (Metadatum r : metadataArray)
                    {
                        if (ii > 0)
                        {
                            resultString += " ";
                        }
                        Choices choices = choice.getBestMatch(field, r.value,
                                colIdx,
                                obtainContext.getCurrentLocale().toString());
                        if (choices != null)
                        {
                            for (Choice ch : choices.values)
                            {
                                resultString += ch.label;
log.debug("Found possible label "+ch.label);
                            }
                        }
                    }
                }
            }
*/

        }
        catch (SQLException | DCInputsReaderException e)
        {
            throw new JspException(e);
        }

        String metadata = "";
        if (metadataArray.length > 0)
        {
            String creativecommons = "";
            String creativecommonslink = null;
            String fulllabel = resultString;
/*
            String[] splittedResult = StringUtils.split(resultString);
            int indexSplit = 0;
            String label = "";
            String splitlabel = resultString;
            String labelAll = "";
            for (int idxs = 0; idxs < splittedResult.length; idxs++) {
                labelAll += " "+splittedResult[idxs];
            }
*/
            for (Metadatum cc : metadataArray) {
//                if(indexSplit < splittedResult.length) {
//                    label = splittedResult[indexSplit];
//                }
                if (cc.value.length() >= 27 && (cc.value.substring(7,26).equals("creativecommons.org") || cc.value.substring(8,27).equals("creativecommons.org"))) {
                    String[] creativecommonsArray = cc.value.split("/");
                    if (creativecommonsArray[creativecommonsArray.length-1].equals("deed.de")) {
                        creativecommons = creativecommonsArray[creativecommonsArray.length-3]+"/"+creativecommonsArray[creativecommonsArray.length-2];
                    } else if (creativecommonsArray[creativecommonsArray.length-1].equals("de")) {
                        creativecommons = creativecommonsArray[creativecommonsArray.length-3]+"/"+creativecommonsArray[creativecommonsArray.length-2];
                    } else {
                        creativecommons = creativecommonsArray[creativecommonsArray.length-2]+"/"+creativecommonsArray[creativecommonsArray.length-1];
                    }
                    creativecommonslink = cc.value;
                }
                if (cc.value.equals("http://rightsstatements.org/vocab/InC/1.0/")) {
                    // copyright
//                    metadata = "<a href='http://rightsstatements.org/vocab/InC/1.0/' target='_blank'><img src='/image/InC.Icon-Only.dark.png' alt='"+fulllabel+"' style='height:21px' /> Result before split: "+fulllabel+" Concatenated Split: "+labelAll+" Split: "+label+"</a>";
                    metadata = "<a href='http://rightsstatements.org/vocab/InC/1.0/' target='_blank'><img src='/image/InC.Icon-Only.dark.png' alt='"+fulllabel+"' style='height:21px' /> "+fulllabel+"</a>";
                }
                else if (creativecommons != "") {
                    if (creativecommonslink.equals("https://creativecommons.org/share-your-work/public-domain/cc0/")) {
                        metadata = "<a href='https://creativecommons.org/share-your-work/public-domain/cc0/'><img src='http://i.creativecommons.org/p/zero/1.0/88x31.png' alt='"+fulllabel+"' title='"+fulllabel+"' /> "+fulllabel+"</a>";
                    } else if (creativecommonslink.equals("https://creativecommons.org/share-your-work/public-domain/pdm/")) {
                        metadata = "<a href='https://creativecommons.org/share-your-work/public-domain/pdm/'><img src='http://i.creativecommons.org/p/mark/1.0/88x31.png' alt='"+fulllabel+"' title='"+fulllabel+"' /> "+fulllabel+"</a>";
                    } else {
//                        metadata = "<a href='"+creativecommonslink+"'><img src='https://licensebuttons.net/l/"+creativecommons+"/88x31.png' alt='"+fulllabel+"' title='"+fulllabel+"' /> Result before split: "+fulllabel+" Concatenated Split: "+labelAll+" Split: "+label+"</a>";
                        metadata = "<a href='"+creativecommonslink+"'><img src='https://licensebuttons.net/l/"+creativecommons+"/88x31.png' alt='"+fulllabel+"' title='"+fulllabel+"' /> "+fulllabel+"</a>";
                    }
                }
                else {
                    // Generic License without Icon
                    if (fulllabel.equals("")) {
                        metadata = "<a href='"+cc.value+"' target='_blank'>"+cc.value+"</a>";
                    } else {
//                        metadata = "<a href='"+cc.value+"' target='_blank'>Result before split: "+fulllabel+" Concatenated Split: "+labelAll+" Split: "+label+"</a>";
                        metadata = "<a href='"+cc.value+"' target='_blank'>Result before split: "+fulllabel+"</a>";
                    }
                }
//                indexSplit++;
            }
        }
        return metadata;
    }

    private String getResult(int colIdx, String field,
            Metadatum[] metadataArray, String resultString, Context obtainContext,
            Collection collection) throws DCInputsReaderException
    {
        String language = I18nUtil.getSupportedLocale(obtainContext.getCurrentLocale()).getLanguage();
log.debug("Using language "+language);
        DCInputSet dcInputSet = valuePairMap.get(language)
                .getInputs(collection.getHandle());
        for (int i = 0; i < dcInputSet.getNumberPages(); i++)
        {
            DCInput[] dcInput = dcInputSet.getPageRows(i, false, false);
            for (DCInput myInput : dcInput)
            {
                String key = myInput.getPairsType();
                if (StringUtils.isNotBlank(key))
                {
                    
                    String inputField = Utils.standardize(myInput.getSchema(), myInput.getElement(), myInput.getQualifier(), ".");

log.debug("Looking for resultString for field "+field+" in field "+inputField);

                    if (inputField.equals(field))
                    {
                        ChoiceAuthority choice = (ChoiceAuthority) PluginManager
                                .getNamedPlugin(ChoiceAuthority.class, key);
log.debug("Got my field!");

                        int ii = 0;
                        for (Metadatum r : metadataArray)
                        {
log.debug("Looking for metadata value of "+r.value);
                            if (ii > 0)
                            {
                                resultString += " ";
                            }
                            Choices choices = choice.getBestMatch(field,
                                    r.value, colIdx, obtainContext
                                            .getCurrentLocale().toString());
log.debug("Found "+choices.total+" values");
                            if (choices != null)
                            {
                                int iii = 0;
                                for (Choice ch : choices.values)
                                {
                                    resultString += ch.label;
log.debug("Found possible label "+ch.label);
                                    if (iii > 0)
                                    {
                                        resultString += " ";
                                    }
                                    iii++;
                                    // Early exit when a match is found
                                    return resultString;
                                }
                            }
                            ii++;
                        }
                    }
                }
            }
        }
        return resultString;
    }

}
