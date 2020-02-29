/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.zdb.ZDBAuthorityValue;
import org.dspace.authority.zdb.ZDBWithoutIssnService;
import org.dspace.content.authority.Choice;
import org.dspace.content.authority.Choices;
import org.dspace.utils.DSpace;

public class ZDBWithoutIssnAuthority extends DOAuthority {

	private static final int DEFAULT_MAX_ROWS = 10;

	private static Logger log = Logger.getLogger(ZDBWithoutIssnAuthority.class);

	private ZDBWithoutIssnService source = new DSpace().getServiceManager().getServiceByName("ZDBSource", ZDBWithoutIssnService.class);

	@Override
	public Choices getMatches(String field, String query, int collection, int start, int limit, String locale, boolean extra) {
		Choices choices = super.getMatches(field, query, collection, start, limit, locale);
		if (extra) {
			choices = new Choices(addExternalResults(field, query, choices, start, limit<=0?DEFAULT_MAX_ROWS:limit), choices.start, choices.total, choices.confidence, choices.more); 
		}
		return choices;
	}

	@Override
	public Choices getMatches(String field, String query, int collection, int start, int limit, String locale) {
		return getMatches(field, query, collection, start, limit, locale, false);
	}
	
	protected Choice[] addExternalResults(String field, String text, Choices choices, int start, int max) {
		if (source != null) {
			try {
				List<Choice> results = new ArrayList<Choice>();
				List<ZDBAuthorityValue> values = source.list(text, start, max);
				// adding choices loop
				int added = 0;
				for (AuthorityValue val : values) {
					if (added < max) {						
						Map<String, String> extras = val.choiceSelectMap();
						extras.put("insolr", "false");
						extras.put("link", getLink(val));
						StringBuffer sb = new StringBuffer(val.getValue());
						sb.append(" (").append(val.getServiceId()).append(")");
						results.add(new Choice(val.generateString(), sb.toString(), val.getValue(), extras));
						added++;
					}
				}
				return (Choice[])ArrayUtils.addAll(choices.values, results.toArray(new Choice[results.size()]));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		} else {
			log.warn("external source for authority not configured");
		}	
		return choices.values;
	}

	private String getLink(AuthorityValue val) {
		return source.buildDetailsURL(val.getServiceId());
	}

    protected boolean hasExtra()
    {
        return false;
    }
}
