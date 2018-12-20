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

import com.github.tomtung.latex2unicode.DefaultLatexToUnicodeConverter;

import gr.ekt.bte.core.AbstractModifier;
import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.StringValue;
import gr.ekt.bte.core.Value;

/**
 * Converts a string with potentially LaTeX encoded content into a decoded UTF8 string
 *
 * @author Oliver Goldschmidt
 */
public class LatexDecodeModifier extends AbstractModifier
{

    List<String> fieldKeys;
    String separator;

    /**
     * @param name
     */
    public LatexDecodeModifier(String name)
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
        String unicode;
        if (fieldKeys != null)
        {
            for (String key : fieldKeys)
            {
                List<Value> values = record.getValues(key);

                List<Value> newValues = new ArrayList<Value>();

                if (values != null)
                {
                    for (Value value : values)
                    {
                        String valueString = value.getAsString();
                        unicode = DefaultLatexToUnicodeConverter.convert(valueString);
                        newValues.add(new StringValue(unicode));
                    }

                    record.updateField(key, newValues);
                }
            }
        }

        return record;
    }

    public void setFieldKeys(List<String> fieldKeys)
    {
        this.fieldKeys = fieldKeys;
    }

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}
}
