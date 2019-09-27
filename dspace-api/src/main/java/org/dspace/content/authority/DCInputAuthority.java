/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.authority;

import java.util.Iterator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.dspace.app.util.DCInputsReader;
import org.dspace.app.util.DCInputsReaderException;
import org.dspace.core.I18nUtil;
import org.dspace.core.SelfNamedPlugin;

/**
 * ChoiceAuthority source that reads the same input-forms which drive
 * configurable submission.
 *
 * Configuration:
 *   This MUST be configured aas a self-named plugin, e.g.:
 *     plugin.selfnamed.org.dspace.content.authority.ChoiceAuthority = \
 *        org.dspace.content.authority.DCInputAuthority
 *
 * It AUTOMATICALLY configures a plugin instance for each <value-pairs>
 * element (within <form-value-pairs>) of the input-forms.xml.  The name
 * of the instance is the "value-pairs-name" attribute, e.g.
 * the element: <value-pairs value-pairs-name="common_types" dc-term="type">
 * defines a plugin instance "common_types".
 *
 * IMPORTANT NOTE: Since these value-pairs do NOT include authority keys,
 * the choice lists derived from them do not include authority values.
 * So you should not use them as the choice source for authority-controlled
 * fields.
 */
public class DCInputAuthority extends SelfNamedPlugin implements ChoiceAuthority
{
    private static Logger log = Logger.getLogger(DCInputAuthority.class);

    private String values[] = null;
    private String labels[] = null;

    private static Map<String, DCInputsReader> dcInputsReader = new HashMap<>();
    private static Map<String, String[]> valuesMultilang = new HashMap<>();
    private static Map<String, String[]> labelsMultilang = new HashMap<>();
    private static String pluginNames[] = null;

    public DCInputAuthority()
    {
        super();
    }

    public static String[] getPluginNames()
    {
        if (pluginNames == null)
        {
            initPluginNames();
        }
        
        return (String[]) ArrayUtils.clone(pluginNames);
    }

    private static synchronized void initPluginNames()
    {
        if (pluginNames == null)
        {
            
            if(dcInputsReader.isEmpty()) {
                for (Locale locale : I18nUtil.getSupportedLocales())
                {
                    try
                    {
                        dcInputsReader.put(locale.getLanguage(),
                                new DCInputsReader(I18nUtil
                                        .getInputFormsFileName(locale)));
                    }
                    catch (DCInputsReaderException e)
                    {
                        log.error("Failed reading DCInputs initialization: ",
                                e);
                    }
                }
            }

            List<String> names = new ArrayList<String>();
            for(String key : dcInputsReader.keySet()) {
                Iterator pi = dcInputsReader.get(key).getPairsNameIterator();
                while (pi.hasNext())
                {
                    names.add((String)pi.next());
                }
            }
            pluginNames = names.toArray(new String[names.size()]);
            log.debug("Got plugin names = "+Arrays.deepToString(pluginNames));
        }
    }

    // once-only load of values and labels
    private void init(String locale)
    {
        if(StringUtils.isNotBlank(locale)) {
            values = valuesMultilang.get(locale);
        }
        if (values == null)
        {
            String pname = this.getPluginInstanceName();
            List<String> pairs = dcInputsReader.get(locale).getPairs(pname);
            if (pairs != null)
            {
                values = new String[pairs.size()/2];
                labels = new String[pairs.size()/2];
                for (int i = 0; i < pairs.size(); i += 2)
                {
                    labels[i/2] = pairs.get(i);
                    values[i/2] = pairs.get(i+1);
                }
                valuesMultilang.put(locale, values);
                labelsMultilang.put(locale, labels);
                log.debug("Found pairs for name="+pname);
            }
            else
            {
                log.error("Failed to find any pairs for name=" + pname, new IllegalStateException());
            }
        }
    }


    public Choices getMatches(String field, String query, int collection, int start, int limit, String locale)
    {
        init(locale);

        int dflt = -1;
        Choice v[] = new Choice[values.length];
        for (int i = 0; i < values.length; ++i)
        {
            v[i] = new Choice(values[i], valuesMultilang.get(locale)[i], labelsMultilang.get(locale)[i]);
            if (values[i].equalsIgnoreCase(query))
            {
                dflt = i;
            }
        }
        return new Choices(v, 0, v.length, Choices.CF_AMBIGUOUS, false, dflt);
    }

    public Choices getBestMatch(String field, String text, int collection, String locale)
    {
        init(locale);
        for (int i = 0; i < values.length; ++i)
        {
            if (text.equalsIgnoreCase(values[i]))
            {
                Choice v[] = new Choice[1];
                v[0] = new Choice(String.valueOf(i), valuesMultilang.get(locale)[i], labelsMultilang.get(locale)[i]);
                return new Choices(v, 0, v.length, Choices.CF_UNCERTAIN, false, 0);
            }
        }
        return new Choices(Choices.CF_NOTFOUND);
    }

    public String getLabel(String field, String key, String locale)
    {
        init(locale);
        return labels[Integer.parseInt(key)];
    }
}
