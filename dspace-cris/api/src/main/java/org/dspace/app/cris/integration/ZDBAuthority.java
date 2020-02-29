/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.cris.model.ResearchObject;
import org.dspace.app.cris.util.ResearcherPageUtils;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.zdb.ZDBAuthorityValue;
import org.dspace.authority.zdb.ZDBService;
import org.dspace.content.Metadatum;
import org.dspace.content.authority.Choice;
import org.dspace.content.authority.Choices;
import org.dspace.core.I18nUtil;
import org.dspace.utils.DSpace;

public abstract class ZDBAuthority extends DOAuthority {

	private static final int DEFAULT_MAX_ROWS = 10;

	private static Logger log = Logger.getLogger(ZDBAuthority.class);

	private ZDBService source = new DSpace().getServiceManager().getServiceByName("ZDBSource", ZDBService.class);

	private static final String ZDB_IDENTIFIER_FIELD = "journalIssn";
	private static final String ZDB_RELATION_FIELD = "journalRelation";
	private static final String ZDB_TYPE_FIELD = "journalType";
	private static final String JOURNALS_IDENTIFIER_FIELD = "journalsissn";
	private static final String MESSAGE_IDENTIFIER_NOT_FOUND_KEY = "zdbauthority.identifier.notfound";
	private static final String MESSAGE_TYPE_KEY = "zdbauthority.type";
	private static final String MESSAGE_RELATION_KEY = "zdbauthority.relation";
	

	@Override
	public Choices getMatches(String field, String query, int collection, int start, int limit, String locale, boolean extra) {
		Choices choices = super.getMatches(field, query, collection, start, limit, locale);
		if (extra) {
			choices = new Choices(addExternalResults(field, query, choices, start, limit<=0?DEFAULT_MAX_ROWS:limit, locale), choices.start, choices.total, choices.confidence, choices.more); 
		}
		return choices;
	}

	@Override
	public Choices getMatches(String field, String query, int collection, int start, int limit, String locale) {
		return getMatches(field, query, collection, start, limit, locale, true);
	}
	
	protected Choice[] addExternalResults(String field, String text, Choices choices, int start, int max, String locale) {
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
							results.add(new Choice(val.generateString(), getLabel(val, locale), getZDBValue(searchField, val), extras));
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

	private String getLabel(AuthorityValue val, String locale)
    {
        return getIssn(val, locale) + val.getValue() + getOtherInformation(val, locale);
    }

	private String getOtherInformation(AuthorityValue val, String locale)
    {
        String result = "(";
        
        result += val.getServiceId();
        
        List<String> types = val.getOtherMetadata().get(ZDB_TYPE_FIELD);
        if (types != null && !types.isEmpty())
        {
            result += I18nUtil.getMessage(MESSAGE_TYPE_KEY, new String[] {types.get(0)}, LocaleUtils.toLocale(locale));
        }
        
        List<String> relations = val.getOtherMetadata().get(ZDB_RELATION_FIELD);
        if (relations != null && !relations.isEmpty())
        {
            int index = 1;
            for(String relation : relations) {
                result += I18nUtil.getMessage(MESSAGE_RELATION_KEY, new String[] {relation, ""+index}, LocaleUtils.toLocale(locale));
            }
        }
        return result + ")";
    }

    private String getLink(AuthorityValue val) {
		return source.buildDetailsURL(val.getServiceId());
	}

	protected abstract String getZDBSearchField(String field);

	private Map<String, String> getZDBExtra(String field, AuthorityValue val)
	{
        Map<String, String> extras = new HashMap<String,String>();
        List<ZDBExtraMetadataGenerator> generators = new DSpace().getServiceManager().getServicesByType(ZDBExtraMetadataGenerator.class);
        if(generators!=null) {
            for(ZDBExtraMetadataGenerator gg : generators) {
                Map<String, String> extrasTmp = gg.build(val);
                extras.putAll(extrasTmp);
            }
        }
        // make sure that the extra cannot override the selected value
        extras.remove("data-" + field);
        return extras;
	}

	protected abstract String getZDBValue(String searchField, AuthorityValue val);

	@Override
	protected List<Choice> getChoicesFromCrisObject(String locale, ResearchObject cris, Map<String, String> extras) {
		List<CRISExtraBasicMetadataGenerator> ggs = new DSpace().getServiceManager().getServicesByType(CRISExtraBasicMetadataGenerator.class);
		String extraIssn = null;
		for (CRISExtraBasicMetadataGenerator gg : ggs) {
			if (gg.getRelatedMetadata().equalsIgnoreCase("crisjournals." +JOURNALS_IDENTIFIER_FIELD)) {
				extraIssn = gg.getRelatedInputformMetadata();
				break;
			}
		}
		if (extras == null) {
			extras = new HashMap<String,String>();
		}
		List<Choice> choices = new ArrayList<Choice>();
		List<String> issns = ResearcherPageUtils.getStringValues(cris, JOURNALS_IDENTIFIER_FIELD);
		if (issns.size() > 0) {
			for (String issn : issns) {
				Map<String, String> currExtras = extras;
				if (StringUtils.isNotBlank(extraIssn)) {
					currExtras = new HashMap<String, String>();
					currExtras.putAll(extras);
					currExtras.put("data-" + extraIssn, issn);
				}
				Choice choice = new Choice(ResearcherPageUtils
				        .getPersistentIdentifier(cris), 
				        getDisplayEntry(issn, cris, locale), 
				        cris.getName(), currExtras);
				
				choices.add(choice);
			}
		}
		else {
			Choice choice = new Choice(ResearcherPageUtils
			        .getPersistentIdentifier(cris),
			        getDisplayEntry(null, cris, locale), 
			        cris.getName(),extras);
			choices.add(choice);	
		}
		return choices;
	}
	
	protected String getDisplayEntry(String issn, ResearchObject cris, String locale)
	{
		return getIssnOrDefault(issn, locale)
				+ super.getDisplayEntry(cris, locale);
	}

	@Override
	protected String getValue(ResearchObject cris)
	{
		String searchField = getDefaultField();
		if (StringUtils.isNotBlank(searchField))
		{
			Metadatum[] mm = cris.getMetadataByMetadataString(searchField);
			if (mm != null && mm.length > 0)
			{
				return mm[0].value;
			}
		}

		return super.getValue(cris);
	}

    private String getIssn(AuthorityValue val, String locale)
    {
        String issn = "";
        List<String> issns = val.getOtherMetadata().get(ZDB_IDENTIFIER_FIELD);
        if (issns != null && !issns.isEmpty())
        {
            Collections.sort(issns);
            issn = StringUtils.join(issns, "|");
        }
        return getIssnOrDefault(issn, locale);
    }

    private String getIssnOrDefault(String issn, String locale)
    {
        return "[" + (StringUtils.isNotBlank(issn) ? issn : I18nUtil.getMessage(MESSAGE_IDENTIFIER_NOT_FOUND_KEY, LocaleUtils.toLocale(locale))) + "] ";
    }
}
