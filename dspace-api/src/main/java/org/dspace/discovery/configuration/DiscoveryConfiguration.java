/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery.configuration;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Kevin Van de Velde (kevin at atmire dot com)
 */
public class DiscoveryConfiguration implements InitializingBean{

	public static final String GLOBAL_CONFIGURATIONNAME = "global";
	
    /** The configuration for the sidebar facets **/
    private List<DiscoverySearchFilterFacet> sidebarFacets = new ArrayList<DiscoverySearchFilterFacet>();
    
    private TagCloudFacetConfiguration tagCloudFacetConfiguration = new TagCloudFacetConfiguration();
    
    /** The default filter queries which will be applied to any search & the recent submissions **/
    private List<String> defaultFilterQueries;

    //TODO refactoring name in plugin processor configuration
    /** Configuration object for the recent submissions **/
    private DiscoveryRecentSubmissionsConfiguration recentSubmissionConfiguration;

    private DiscoveryMostViewedConfiguration mostViewConfiguration;
    
    /** The search filters which can be selected on the search page**/
    private List<DiscoverySearchFilter> searchFilters = new ArrayList<DiscoverySearchFilter>();

    private DiscoverySortConfiguration searchSortConfiguration;

    private int defaultRpp = 10;
    private int maxRpp = -1;
    
    private String id;
    private DiscoveryHitHighlightingConfiguration hitHighlightingConfiguration;
    private DiscoveryMoreLikeThisConfiguration moreLikeThisConfiguration;
    private DiscoveryCollapsingConfiguration collapsingConfiguration;
    
	private boolean spellCheckEnabled;
	
	private boolean globalConfigurationEnabled;
	
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<DiscoverySearchFilterFacet> getSidebarFacets() {
        return sidebarFacets;
    }

    public void setSidebarFacets(List<DiscoverySearchFilterFacet> sidebarFacets) {
        this.sidebarFacets = sidebarFacets;
    }

    public TagCloudFacetConfiguration getTagCloudFacetConfiguration() {
		return tagCloudFacetConfiguration;
	}

	public void setTagCloudFacetConfiguration(TagCloudFacetConfiguration tagCloudFacetConfiguration) {
		this.tagCloudFacetConfiguration = tagCloudFacetConfiguration;
	}

	public List<String> getDefaultFilterQueries() {
        //Since default filter queries are not mandatory we will return an empty list
        if(defaultFilterQueries == null){
            return new ArrayList<String>();
        }else{
            return defaultFilterQueries;
        }
    }

    public void setDefaultFilterQueries(List<String> defaultFilterQueries) {
        this.defaultFilterQueries = defaultFilterQueries;
    }

    public DiscoveryRecentSubmissionsConfiguration getRecentSubmissionConfiguration() {
        return recentSubmissionConfiguration;
    }

    public void setRecentSubmissionConfiguration(DiscoveryRecentSubmissionsConfiguration recentSubmissionConfiguration) {
        this.recentSubmissionConfiguration = recentSubmissionConfiguration;
    }

    public List<DiscoverySearchFilter> getSearchFilters() {
        return searchFilters;
    }
    
    public void setSearchFilters(List<DiscoverySearchFilter> searchFilters) {
        this.searchFilters = searchFilters;
    }

    public DiscoverySortConfiguration getSearchSortConfiguration() {
        return searchSortConfiguration;
    }

    public void setSearchSortConfiguration(DiscoverySortConfiguration searchSortConfiguration) {
        this.searchSortConfiguration = searchSortConfiguration;
    }
    
    public void setDefaultRpp(int defaultRpp)
    {
        this.defaultRpp = defaultRpp;
    }

    public void setMaxRpp(int maxRpp)
    {
        this.maxRpp = maxRpp;
    }
    
    public int getDefaultRpp()
    {
        return defaultRpp;
    }

    public int getMaxRpp()
    {
        return (maxRpp < 0) ? (2 * defaultRpp) : maxRpp;
    }

    public void setHitHighlightingConfiguration(DiscoveryHitHighlightingConfiguration hitHighlightingConfiguration) {
        this.hitHighlightingConfiguration = hitHighlightingConfiguration;
    }

    public DiscoveryHitHighlightingConfiguration getHitHighlightingConfiguration() {
        return hitHighlightingConfiguration;
    }

    public void setMoreLikeThisConfiguration(DiscoveryMoreLikeThisConfiguration moreLikeThisConfiguration) {
        this.moreLikeThisConfiguration = moreLikeThisConfiguration;
    }

    public DiscoveryMoreLikeThisConfiguration getMoreLikeThisConfiguration() {
        return moreLikeThisConfiguration;
    }

    public boolean isSpellCheckEnabled() {
        return spellCheckEnabled;
    }

    public void setSpellCheckEnabled(boolean spellCheckEnabled) {
        this.spellCheckEnabled = spellCheckEnabled;
    }

    /**
     * After all the properties are set check that the sidebar facets are a subset of our search filters
     *
     * @throws Exception throws an exception if this isn't the case
     */
    @Override
	public void afterPropertiesSet() throws Exception {

		
		if (getSearchFilters() != null && getSidebarFacets() != null) {			
			Collection missingSearchFilters = CollectionUtils.subtract(getSidebarFacets(), getSearchFilters());
			if (CollectionUtils.isNotEmpty(missingSearchFilters)) {
				StringBuilder error = new StringBuilder();
				error.append("The following sidebar facet configurations are not present in the search filters list: ");
				for (Object missingSearchFilter : missingSearchFilters) {
					DiscoverySearchFilter searchFilter = (DiscoverySearchFilter) missingSearchFilter;
					error.append(searchFilter.getIndexFieldName()).append(" ");
				}
				
				throw new DiscoveryConfigurationException(error.toString());
			}
		}
        Collection missingTagCloudSearchFilters = CollectionUtils.subtract(getTagCloudFacetConfiguration().getTagCloudFacets(), getSearchFilters());
        if(CollectionUtils.isNotEmpty(missingTagCloudSearchFilters))
        {
            StringBuilder error = new StringBuilder();
            error.append("The following tagCloud facet configurations are not present in the search filters list: ");
            for (Object missingSearchFilter : missingTagCloudSearchFilters)
            {
                DiscoverySearchFilter searchFilter = (DiscoverySearchFilter) missingSearchFilter;
                error.append(searchFilter.getIndexFieldName()).append(" ");

            }
            error.append("all the tagCloud facets MUST be a part of the search filters list.");

			throw new DiscoveryConfigurationException(error.toString());			
		}
	}
    
    public DiscoveryCollapsingConfiguration getCollapsingConfiguration() {
		return collapsingConfiguration;
	}

	public void setCollapsingConfiguration(DiscoveryCollapsingConfiguration collapsingConfiguration) {
		this.collapsingConfiguration = collapsingConfiguration;
	}

	public boolean isGlobalConfigurationEnabled() {
		return globalConfigurationEnabled;
	}
	
	public void setGlobalConfigurationEnabled(boolean globalConfigurationEnabled) {
		this.globalConfigurationEnabled = globalConfigurationEnabled;
	}

    public DiscoveryMostViewedConfiguration getMostViewConfiguration()
    {
        return mostViewConfiguration;
    }

    public void setMostViewConfiguration(
            DiscoveryMostViewedConfiguration mostViewConfiguration)
    {
        this.mostViewConfiguration = mostViewConfiguration;
    }

}