/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.integration;

import java.sql.SQLException;

import org.dspace.app.util.MappingMetadata;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;

import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * Based on GoogleMetadata crosswalk
 * 
 */
public class DatasetSchemaOrgMetadata extends MappingMetadata
{

    private static final String CONTEXT_VALUE = "https://schema.org/";

    private static final String TYPE_VALUE = "Dataset";

    private static final String DATASET_PREFIX = "schemaorg.dataset.";

    public static final String CONTEXT = "@context";
    
    public static final String TYPE = "@type";
    
    public static final String TITLE = "name";
    
    public static final String AUTHOR = "author";
    
    public static final String DESCRIPTION = "description";
    
    public static final String URL = "url";
    
    public static final String SAME_AS = "sameAs";
    
    public static final String IDENTIFIER = "identifier";
    
    public static final String KEYWORDS = "keywords";
    
    public static final String LICENSE = "license";
    
    public static final String HAS_PART = "hasPart";
    
    public static final String CREATOR = "creator";
    
    public static final String FUNDER = "funder";
    
    public static final String INCLUDED_IN_DATA_CATALOG = "includedInDataCatalog";
    
    public static final String DISTRIBUTION = "distribution";
    
    public static final String TEMPORAL_COVERAGE = "temporalCoverage";
    
    public static final String SPATIAL_COVERAGE = "spatialCoverage";
    
    private Gson gson = new GsonBuilder().enableComplexMapKeySerialization()
            .setPrettyPrinting().create();
    
    private JsonObject jsonObject;
    /**
     * Wrap the item, parse all configured fields and generate metadata field
     * values.
     * 
     * @param item
     *            - The item being viewed to extract metadata from
     */
    public DatasetSchemaOrgMetadata(Context context, Item item)
            throws SQLException
    {
        init("schemaorg-dataset-metadata.config");
        // Hold onto the item in case we need to refresh a stale parse
        this.item = item;
        itemURL = HandleManager.resolveToURL(context, item.getHandle());
        parseItem();
    }

    /**
     * Using metadata field mappings contained in the loaded configuration,
     * parse through configured metadata fields, building valid Google metadata
     * value strings. Field names & values contained in metadataMappings.
     * 
     */
    private void parseItem()
    {
        jsonObject = new JsonObject();
        
        getMetadataMapping().put(CONTEXT, CONTEXT_VALUE);        
        jsonObject.addProperty(CONTEXT, CONTEXT_VALUE);
        
        getMetadataMapping().put(TYPE, TYPE_VALUE);
        jsonObject.addProperty(TYPE, TYPE_VALUE);
        
        if(addSingleField(TITLE)) {        
            jsonObject.addProperty(TITLE, getMetadataMapping().get(TITLE).get(0));
        }
        if(addDisseminatedValue(AUTHOR)) {
            String author = getMetadataMapping().get(AUTHOR).get(0);
            JsonObject internalJsonObject = new JsonParser().parse(author).getAsJsonObject();
            jsonObject.add(AUTHOR, internalJsonObject);
        }
        if(addSingleField(DESCRIPTION)) {
            jsonObject.addProperty(DESCRIPTION, getMetadataMapping().get(DESCRIPTION).get(0));
        }
        if(addSingleField(URL)) {
            jsonObject.addProperty(URL, getMetadataMapping().get(URL).get(0));
        }
        if(addSingleField(SAME_AS)) {
            jsonObject.addProperty(SAME_AS, getMetadataMapping().get(SAME_AS).get(0));
        }        
        if(addMultipleValues(IDENTIFIER)) {
            jsonObject.add(IDENTIFIER, gson.toJsonTree(getMetadataMapping().get(IDENTIFIER)));
        }
        if(addMultipleValues(KEYWORDS)) {
            jsonObject.add(KEYWORDS, gson.toJsonTree(getMetadataMapping().get(KEYWORDS)));
        }
        if(addSingleField(LICENSE)) {
            jsonObject.addProperty(LICENSE, getMetadataMapping().get(LICENSE).get(0));
        }
        if(addDisseminatedValue(HAS_PART)) {
            String haspart = getMetadataMapping().get(HAS_PART).get(0);
            JsonObject internalJsonObject = new JsonParser().parse(haspart).getAsJsonObject();
            jsonObject.add(HAS_PART, internalJsonObject);
        }
        if(addDisseminatedValue(CREATOR)) {
            String creator = getMetadataMapping().get(CREATOR).get(0);
            JsonObject internalJsonObject = new JsonParser().parse(creator).getAsJsonObject();
            jsonObject.add(CREATOR, internalJsonObject);
        }
        if(addDisseminatedValue(FUNDER)) {
            String funder = getMetadataMapping().get(FUNDER).get(0);
            JsonObject internalJsonObject = new JsonParser().parse(funder).getAsJsonObject();
            jsonObject.add(FUNDER, internalJsonObject);
        }
        if(addDisseminatedValue(INCLUDED_IN_DATA_CATALOG)) {
            String included = getMetadataMapping().get(INCLUDED_IN_DATA_CATALOG).get(0);
            JsonObject internalJsonObject = new JsonParser().parse(included).getAsJsonObject();
            jsonObject.add(INCLUDED_IN_DATA_CATALOG, internalJsonObject);
        }
        if(addDisseminatedValue(DISTRIBUTION)) {
            String distribution = getMetadataMapping().get(DISTRIBUTION).get(0);
            JsonObject internalJsonObject = new JsonParser().parse(distribution).getAsJsonObject();
            jsonObject.add(DISTRIBUTION, internalJsonObject);
        }
        if(addSingleField(TEMPORAL_COVERAGE)) {
            jsonObject.addProperty(TEMPORAL_COVERAGE, getMetadataMapping().get(TEMPORAL_COVERAGE).get(0));
        }
        if(addDisseminatedValue(SPATIAL_COVERAGE)) {
            String spatial = getMetadataMapping().get(SPATIAL_COVERAGE).get(0);
            JsonObject internalJsonObject = new JsonParser().parse(spatial).getAsJsonObject();
            jsonObject.add(SPATIAL_COVERAGE, internalJsonObject);
        }        
    }

    @Override
    protected String getPrefix()
    {
        return DATASET_PREFIX;
    }

    public ListMultimap<String, String> getMetadataMapping()
    {
        return metadataMappings;
    }

    public String getJsonAsString()
    {
        return gson.toJson(jsonObject);
    }

    public JsonObject getJsonObject()
    {
        return jsonObject;
    }

    public void setJsonObject(JsonObject jsonObject)
    {
        this.jsonObject = jsonObject;
    }
    
}
