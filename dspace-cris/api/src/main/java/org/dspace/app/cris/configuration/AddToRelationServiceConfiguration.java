/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.configuration;

import java.util.List;

/**
 * This class contains the configuration of all the {@link AddToRelationService}
 * defined in the Spring configuration file:
 * cris-relationpreference.xml
 *
 */
public class AddToRelationServiceConfiguration {

    /** list of all the {@link AddToRelationService} */
    private List<AddToRelationService> list;

    public List<AddToRelationService> getList() {
        return list;
    }

    public void setList(List<AddToRelationService> list) {
        this.list = list;
    }

    public synchronized AddToRelationService getAddToRelationService(String name) {
        for (AddToRelationService relationService : list) {
            if (relationService.getRelationConfiguration() != null) {
                if (name.equals(relationService.getRelationConfiguration().getRelationName())) {
                    return relationService;
                }
            }
        }

        return null;
    }

}
