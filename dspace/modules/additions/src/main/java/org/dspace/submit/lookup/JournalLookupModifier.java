package org.dspace.submit.lookup;

import java.util.ArrayList;
import java.util.List;

import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.util.Util;

public class JournalLookupModifier<T extends ACrisObject>
        extends AuthorityLookupModifier<T>
{
    @Override
    public List<String> normalize(List<String> values)
    {
        List<String> result = new ArrayList<String>();
        for (String value : values)
        {
            result.add(Util.normalizeISSN(value));
        }
        return result;
    }
}
