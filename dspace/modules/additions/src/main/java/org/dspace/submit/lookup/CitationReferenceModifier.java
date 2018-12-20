/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.submit.lookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gr.ekt.bte.core.AbstractModifier;
import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.StringValue;
import gr.ekt.bte.core.Value;

import org.apache.log4j.Logger;

/**
 * builds a string containing metadata values
 *
 * @author Oliver Goldschmidt
 */
public class CitationReferenceModifier extends AbstractModifier
{

    private static Logger logger_ = Logger.getLogger(MetadataToStringModifier.class);
    Map<String, String> fieldKeys;
    String basicString;
    String finalField;

    /**
     * @param name
     */
    public CitationReferenceModifier(String name)
    {
        super(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * gr.ekt.bte.core.AbstractModifier#modify(gr.ekt.bte.core.MutableRecord)
     */
    @Override
    public Record modify(MutableRecord record)
    {
        if (fieldKeys != null)
        {
            String bs = basicString;
//Basic string is: %%journalname%% %%journalissue%% (%%journalvolume%%): %%journalfirstpage%%-%%journallastpage%% (%%journalyear%%)
            for (Map.Entry<String, String> e : fieldKeys.entrySet())
            {
                List<Value> values = record.getValues(e.getValue());
                String key = e.getKey();
                if (key.equals("journalname") && (values == null || values.isEmpty())) {
                    // forget everything, leave field untouched
                    logger_.debug(key + " is empty. Leaving "+finalField+" untouched.");
                    bs = "";
                    break;
                }

                logger_.debug("Loading value for field: " + key);
                if (values == null || values.isEmpty()) {
                    bs = bs.replace("%%"+key+"%%", "");
                }
                else {
                    for (Value value : values)
                    {
                        logger_.debug("Got value: " + value.getAsString());
                        bs = bs.replace("%%"+key+"%%", value.getAsString());
                    }
                }
                logger_.debug("String is now: " + bs);
            }
            if (!bs.equals("")) {
                List<Value> final_value = new ArrayList<Value>();
                final_value.add(new StringValue(bs));
                logger_.debug("Adding " + bs + " to field " + finalField);
                record.updateField(finalField, final_value);
            }
        }

        return record;
    }

    public void setFieldKeys(Map<String, String> fieldKeys)
    {
        this.fieldKeys = fieldKeys;
    }

    public String getBasicString() {
        return basicString;
    }

    public void setBasicString(String basicString) {
        this.basicString = basicString;
    }

    public String getFinalField() {
        return finalField;
    }

    public void setFinalField(String finalField) {
        this.finalField = finalField;
    }
}
