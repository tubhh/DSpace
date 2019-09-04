/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.cris.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.dspace.app.cris.configuration.AddToRelationServiceConfiguration;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.app.cris.util.ResearcherPageUtils;
import org.dspace.app.webui.discovery.DiscoverUtility;
import org.dspace.app.webui.servlet.DSpaceServlet;
import org.dspace.app.webui.util.JSPManager;
import org.dspace.app.webui.util.UIUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.browse.BrowsableDSpaceObject;
import org.dspace.browse.BrowseDSpaceObject;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.I18nUtil;
import org.dspace.core.LogManager;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverResult;
import org.dspace.discovery.SearchServiceException;
import org.dspace.discovery.SearchUtils;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.dspace.discovery.configuration.DiscoverySearchFilter;
import org.dspace.discovery.configuration.DiscoverySearchFilterFacet;
import org.dspace.discovery.configuration.DiscoverySortFieldConfiguration;
import org.dspace.utils.DSpace;

public class AddToRelationServlet extends DSpaceServlet {

    private Logger log = Logger.getLogger(AddToRelationServlet.class);

    private DSpace dspace = new DSpace();

    private ApplicationService applicationService = dspace
            .getServiceManager()
            .getServiceByName(
                    "applicationService",
                    ApplicationService.class);

    private AddToRelationServiceConfiguration addToRelationServiceConfiguration = dspace
            .getServiceManager()
            .getServiceByName(
                    AddToRelationServiceConfiguration.class.getName(),
                    AddToRelationServiceConfiguration.class);

    @Override
    protected void doDSGet(Context context, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            SQLException, AuthorizeException
    {
        doDSPost(context, request, response);
    }

    protected void doDSPost(Context context, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            SQLException, AuthorizeException
    {
        String crisID = request.getParameter("crisID");
        String relationName = request.getParameter("relationName");

        // check authorization
        if (!addToRelationServiceConfiguration
                .getAddToRelationService(relationName)
                .isAuthorized(context, applicationService.getEntityByCrisId(crisID))) {
            throw new AuthorizeException("The user is not allow to manage the relation " + relationName + " for CRISObject " + crisID);
        }

        ACrisObject cris = applicationService.getEntityByCrisId(crisID);
        request.setAttribute("crisObject", cris);
        String publicPath = request.getContextPath() + "/cris/" + cris.getPublicPath() + "/" + ResearcherPageUtils.getPersistentIdentifier(cris);

        String configurationName = request.getParameter("location");
        if (StringUtils.isBlank(configurationName)) {
            if (StringUtils.isNotBlank(relationName) && relationName.contains(".")) {
                configurationName = relationName.split("\\.")[1];
            }
        }

        DSpaceObject selectedObject = null;
        String selectedPublicPath = "";
        String ammissibleQuery = "";
        String notAmmissibleMessage = "";
        List<String> addedObjectIDs = retrieveAddedObjectIDs(context, request, cris, relationName, configurationName);
        if (StringUtils.isNotBlank(request.getParameter("selected-itemID"))
                && !addedObjectIDs.contains(request.getParameter("selected-itemID"))) {
            selectedObject = Item.find(context, Integer.valueOf(request.getParameter("selected-itemID")));
            if (selectedObject != null) {
                selectedPublicPath = request.getContextPath() + "/handle/" + selectedObject.getHandle();
                ammissibleQuery = "search.resourcetype:"
                        + selectedObject.getType()
                        + " AND search.resourceid:"
                        + selectedObject.getID();
                notAmmissibleMessage = "Item with ID " + selectedObject.getID();
            }
            else {
                log.warn("The user try to add inexistent Item with ID " + request.getParameter("selected-itemID"));
            }
        }
        else if (StringUtils.isNotBlank(request.getParameter("selected-crisID"))
                && !addedObjectIDs.contains(request.getParameter("selected-crisID"))) {
            selectedObject = applicationService.getEntityByCrisId(request.getParameter("selected-crisID"));
            if (selectedObject != null) {
                selectedPublicPath = request.getContextPath() + "/cris/" + ((ACrisObject)selectedObject).getPublicPath() + "/" + ResearcherPageUtils.getPersistentIdentifier(((ACrisObject)selectedObject));
                ammissibleQuery = "cris-id:"
                        + ((ACrisObject)selectedObject).getCrisID();
                notAmmissibleMessage = "CRISObject " + ((ACrisObject)selectedObject).getCrisID();
            }
            else {
                log.warn("The user try to add inexistent CRISObject " + request.getParameter("selected-crisID"));
            }
        }
        if (selectedObject != null) {
            if (isAmmissibleObject(context, request, relationName, ammissibleQuery)) {
                if (addToRelationServiceConfiguration
                        .getAddToRelationService(relationName)
                        .executeAction(cris, selectedObject)) {
                    addMessage(context, request, cris, publicPath, selectedObject, selectedPublicPath);
                    context.commit();
                    response.sendRedirect(publicPath);
                    return;
                }
            }
            else {
                log.warn("The user try to add not ammissible " + notAmmissibleMessage + " for the relation " + relationName);
            }
        }

        request.setAttribute("searchName", request.getContextPath() + request.getServletPath());

        Item[] resultsItems;
        DSpaceObject scope = null;
        try {
            scope = DiscoverUtility.getSearchScope(context, request);
        } catch (IllegalStateException | SQLException e) {
            log.error(e.getMessage(), e);
        }

        DiscoveryConfiguration discoveryConfiguration = addToRelationServiceConfiguration
                .getAddToRelationService(relationName)
                .getDiscoveryConfiguration();

        List<DiscoverySortFieldConfiguration> sortFields = discoveryConfiguration.getSearchSortConfiguration()
                .getSortFields();
        List<String> sortOptions = new ArrayList<String>();
        for (DiscoverySortFieldConfiguration sortFieldConfiguration : sortFields) {
            String sortField = SearchUtils.getSearchService().toSortFieldIndex(
                    sortFieldConfiguration.getMetadataField(), sortFieldConfiguration.getType());
            sortOptions.add(sortField);
        }
        request.setAttribute("sortOptions", sortOptions);

        DiscoverQuery queryArgs = DiscoverUtility.getDiscoverQuery(context, request, scope, configurationName, true);

        queryArgs.setSpellCheck(discoveryConfiguration.isSpellCheckEnabled());

        List<DiscoverySearchFilterFacet> availableFacet = discoveryConfiguration.getSidebarFacets();

        request.setAttribute("facetsConfig", availableFacet != null ? availableFacet
                : new ArrayList<DiscoverySearchFilterFacet>());

        int etal = UIUtil.getIntParameter(request, "etal");
        if (etal == -1) {
            etal = ConfigurationManager.getIntProperty("webui.itemlist.author-limit");
        }

        request.setAttribute("etal", etal);

        String query = queryArgs.getQuery();
        request.setAttribute("query", query);
        request.setAttribute("queryArgs", queryArgs);
        List<DiscoverySearchFilter> availableFilters = discoveryConfiguration.getSearchFilters();
        request.setAttribute("availableFilters", availableFilters);

        List<String[]> appliedFilters = DiscoverUtility.getFilters(request, null);
        request.setAttribute("appliedFilters", appliedFilters);
        List<String> appliedFilterQueries = new ArrayList<String>();
        for (String[] filter : appliedFilters) {
            appliedFilterQueries.add(filter[0] + "::" + filter[1] + "::" + filter[2]);
        }
        request.setAttribute("appliedFilterQueries", appliedFilterQueries);
        List<DSpaceObject> scopes = new ArrayList<DSpaceObject>();
        if (scope == null) {
            Community[] topCommunities = new Community[0];
            try {
                topCommunities = Community.findAllTop(context);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
            for (Community com : topCommunities) {
                scopes.add(com);
            }
        } else {
            try {
                DSpaceObject pDso = scope.getParentObject();
                while (pDso != null) {
                    // add to the available scopes in reverse order
                    scopes.add(0, pDso);
                    pDso = pDso.getParentObject();
                }
                scopes.add(scope);
                if (scope instanceof Community) {
                    Community[] comms = ((Community) scope).getSubcommunities();
                    for (Community com : comms) {
                        scopes.add(com);
                    }
                    Collection[] colls = ((Community) scope).getCollections();
                    for (Collection col : colls) {
                        scopes.add(col);
                    }
                }
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
        request.setAttribute("scope", scope);
        request.setAttribute("scopes", scopes);

        // Perform the search
        DiscoverResult qResults = null;
        try {
            qResults = SearchUtils.getSearchService().search(context, scope, queryArgs);

            List<Item> resultsListItem = new ArrayList<Item>();

            Map<Integer, List<DSpaceObject>> resultsListOther = new HashMap<Integer, List<DSpaceObject>>();

            Set<Integer> resultsSortedSetObjectsType = new LinkedHashSet<Integer>();

            for (DSpaceObject dso : qResults.getDspaceObjects()) {
                if (dso instanceof Item) {
                    resultsListItem.add((Item) dso);
                } else if (dso instanceof BrowsableDSpaceObject) {
                    List<DSpaceObject> currList = resultsListOther.get(dso.getType());
                    if (currList != null) {
                        currList.add(new BrowseDSpaceObject(context, (BrowsableDSpaceObject) dso));
                    } else {
                        List<DSpaceObject> newlist = new ArrayList<DSpaceObject>();
                        resultsListOther.put(dso.getType(), newlist);
                        newlist.add(new BrowseDSpaceObject(context, (BrowsableDSpaceObject) dso));
                    }
                }

                resultsSortedSetObjectsType.add(dso.getType());
            }

            // Make objects from the handles - make arrays, fill them out
            resultsItems = new Item[resultsListItem.size()];
            Map<Integer, BrowseDSpaceObject[]> resultsMapOthers = new HashMap<Integer, BrowseDSpaceObject[]>();
            for (Integer key : resultsListOther.keySet()) {
                BrowseDSpaceObject[] resultsOther = new BrowseDSpaceObject[resultsListOther.get(key).size()];
                resultsOther = resultsListOther.get(key).toArray(resultsOther);
                resultsMapOthers.put(key, resultsOther);
            }
            resultsItems = resultsListItem.toArray(resultsItems);

            Integer[] resultsSortedObjectsType = new Integer[resultsSortedSetObjectsType.size()];
            resultsSortedObjectsType = resultsSortedSetObjectsType.toArray(resultsSortedObjectsType);

            // Pass in some page qualities
            // total number of pages
            long pageTotal = 1 + ((qResults.getTotalSearchResults() - 1) / qResults.getMaxResults());

            // current page being displayed
            long pageCurrent = 1 + (qResults.getStart() / qResults.getMaxResults());

            // pageLast = min(pageCurrent+3,pageTotal)
            long pageLast = ((pageCurrent + 3) > pageTotal) ? pageTotal : (pageCurrent + 3);

            // pageFirst = max(1,pageCurrent-3)
            long pageFirst = ((pageCurrent - 3) > 1) ? (pageCurrent - 3) : 1;

            // Pass the results to the display JSP
            request.setAttribute("items", resultsItems);
            request.setAttribute("communities", new Community[0]);
            request.setAttribute("collections", new Collection[0]);
            request.setAttribute("resultsMapOthers", resultsMapOthers);
            request.setAttribute("sortedObjectsType", resultsSortedObjectsType);
            request.setAttribute("pagetotal", new Long(pageTotal));
            request.setAttribute("pagecurrent", new Long(pageCurrent));
            request.setAttribute("pagelast", new Long(pageLast));
            request.setAttribute("pagefirst", new Long(pageFirst));
            request.setAttribute("spellcheck", qResults.getSpellCheckQuery());
            request.setAttribute("queryresults", qResults);
        } catch (SearchServiceException e) {
            log.error(
                    LogManager.getHeader(context, "search", "query=" + queryArgs.getQuery() + ",scope=" + scope
                            + ",error=" + e.getMessage()), e);
            request.setAttribute("search.error", true);
            request.setAttribute("search.error.message", e.getMessage());
        }

        JSPManager.showJSP(request, response, "/search/discovery.jsp?location=" + configurationName + "&crisID=" + crisID + "&relationName=" + relationName);
    }

    private List<String> retrieveAddedObjectIDs(Context context, HttpServletRequest request, ACrisObject cris, String relationName, String configurationName) {
        List<String> addedObjectIDs = new ArrayList<>();

        DiscoveryConfiguration discoveryConfiguration = addToRelationServiceConfiguration
                .getAddToRelationService(relationName)
                .getDiscoveryConfiguration();

        String relationQuery = MessageFormat.format(addToRelationServiceConfiguration
                .getAddToRelationService(relationName).getRelationConfiguration().getQuery(), cris.getCrisID(), cris.getUuid());

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRows(Integer.MAX_VALUE);
        solrQuery.setQuery(relationQuery);

        List<String> defaultFilterQueries = discoveryConfiguration
                .getDefaultFilterQueries();
        if (defaultFilterQueries != null) {
            for (String f : defaultFilterQueries) {
                solrQuery.addFilterQuery(f);
            }
        }

        solrQuery.addField("search.resourcetype");
        solrQuery.addField("search.resourceid");
        solrQuery.addField("cris-id");

        // Perform the search
        QueryResponse qResponse = null;
        try {
            qResponse = SearchUtils.getSearchService().search(solrQuery);
            if (qResponse.getResults() != null && !qResponse.getResults().isEmpty()) {
                String addedObjectType = "";
                if ((Integer)qResponse.getResults().get(0).get("search.resourcetype") == Constants.ITEM) {
                    addedObjectType = "added-itemIDs";
                    for (SolrDocument sd : qResponse.getResults()) {
                        addedObjectIDs.add(String.valueOf(sd.get("search.resourceid")));
                    }
                }
                else {
                    addedObjectType = "added-crisIDs";
                    for (SolrDocument sd : qResponse.getResults()) {
                        addedObjectIDs.add((String)sd.get("cris-id"));
                    }
                }
                request.setAttribute(addedObjectType, Arrays.toString(addedObjectIDs.toArray()).replaceAll("\\[|\\]| ", ""));
            }
        } catch (SearchServiceException e) {
            log.error(e.getMessage(), e);
        }

        return addedObjectIDs;
    }

    private boolean isAmmissibleObject(Context context, HttpServletRequest request, String relationName, String selectedQuery) {
        DiscoveryConfiguration discoveryConfiguration = addToRelationServiceConfiguration
                .getAddToRelationService(relationName)
                .getDiscoveryConfiguration();

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("*:*");

        List<String> defaultFilterQueries = discoveryConfiguration
                .getDefaultFilterQueries();
        if (defaultFilterQueries != null) {
            for (String f : defaultFilterQueries) {
                solrQuery.addFilterQuery(f);
            }
        }

        solrQuery.addFilterQuery(selectedQuery);

        // Perform the search
        QueryResponse qResponse = null;
        try {
            qResponse = SearchUtils.getSearchService().search(solrQuery);
            return qResponse.getResults() != null && qResponse.getResults().getNumFound() == 1;
        } catch (SearchServiceException e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    private void addMessage(Context context, HttpServletRequest request, DSpaceObject target, String targetPath, DSpaceObject selected, String selectedPath) {
        List messages = (List) request.getSession().getAttribute(
                it.cilea.osd.common.constants.Constants.MESSAGES_KEY);
        if (messages == null) {
            messages = new ArrayList();
        }
        messages.add(
                I18nUtil.getMessage(
                        "jsp.layout.cris.addrelations.success.info",
                        new String[] {
                                targetPath,
                                target.getName(),
                                selectedPath,
                                selected.getName()
                        },
                        context));
        request.getSession().setAttribute(it.cilea.osd.common.constants.Constants.MESSAGES_KEY, messages);
    }

}
