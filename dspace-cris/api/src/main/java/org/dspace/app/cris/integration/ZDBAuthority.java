/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.zdb.ZDBAuthorityValue;
import org.dspace.authority.zdb.ZDBService;
import org.dspace.content.authority.Choice;
import org.dspace.content.authority.Choices;
import org.dspace.core.ConfigurationManager;
import org.dspace.utils.DSpace;

public class ZDBAuthority extends DOAuthority {

	private static final int DEFAULT_MAX_ROWS = 10;

	private static Logger log = Logger.getLogger(ZDBAuthority.class);

	private ZDBService source = new DSpace().getServiceManager().getServiceByName("ZDBSource", ZDBService.class);

	private ZDBExtraConfiguration zdbExtraConfiguration = new DSpace().getServiceManager().getServiceByName("ZDBExtraConfiguration", ZDBExtraConfiguration.class);

	@Override
	public Choices getMatches(String field, String query, int collection, int start, int limit, String locale) {
		Choices choices = super.getMatches(field, query, collection, start, limit, locale);		
		return new Choices(addExternalResults(field, query, choices, start, limit<=0?DEFAULT_MAX_ROWS:limit), choices.start, choices.total, choices.confidence, choices.more);
	}

	protected Choice[] addExternalResults(String field, String text, Choices choices, int start, int max) {
		if (source != null) {
			try {
				List<Choice> results = new ArrayList<Choice>();
				String searchField = getZDBSearchField(field);
				if (StringUtils.isNotBlank(searchField))
				{
					List<ZDBAuthorityValue> values = source.list(searchField, text, start, max);
					// adding choices loop
					int added = 0;
					for (AuthorityValue val : values) {
						if (added < max) {
							Map<String, String> extras = val.choiceSelectMap();
							extras.put("insolr", "false");
							extras.put("link", getLink(val));
							extras.putAll(getZDBExtra(field, val));
							results.add(new Choice(val.generateString(), val.getValue(), getZDBValue(searchField, val), extras));
							added++;
						}
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

	private String getZDBSearchField(String field)
	{
		if (ConfigurationManager.getBooleanProperty(field + ".search.title", false))
		{
			return "tit";
		}
		else if (ConfigurationManager.getBooleanProperty(field + ".search.issn", false))
		{
			return "iss";
		}
		return null;
	}

	private Map<String, String> getZDBExtra(String field, AuthorityValue val)
	{
		Map<String, String> extras = new HashMap<String,String>();

		if (zdbExtraConfiguration != null)
		{
			for (ZDBExtraMetadataGenerator gg : zdbExtraConfiguration.getExtraMap().get(field))
			{
				extras.putAll(gg.buildExtra(val));
			}
		}

		return extras;
	}

	private String getZDBValue(String searchField, AuthorityValue val)
	{
		if (searchField.equals("iss"))
		{
			List<String> issns = val.getOtherMetadata().get("journalIssn");
			if (issns != null && !issns.isEmpty())
			{
				return issns.get(0);
			}
		}
		// default get title
		return val.getValue();
	}
}
