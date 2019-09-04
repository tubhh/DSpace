/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.cris.configuration;

import java.util.List;

import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.discovery.configuration.DiscoveryConfiguration;

public class AddToRelationService {

    private RelationConfiguration relationConfiguration;
    private DiscoveryConfiguration discoveryConfiguration;

    private RelationMetadataAction action;
    private List<SecurityCheck> security;

    public boolean isAuthorized(Context context, DSpaceObject dso) {
        for (SecurityCheck securityCheck : security) {
            if (securityCheck.isAuthorized(context, dso)) {
                return true;
            }
        }

        return false;
    }

    public boolean executeAction(DSpaceObject target, DSpaceObject selected)
    {
        return action.processSelectedItem(target, selected);
    }

    public RelationConfiguration getRelationConfiguration() {
        return relationConfiguration;
    }

    public void setRelationConfiguration(RelationConfiguration relationConfiguration) {
        this.relationConfiguration = relationConfiguration;
    }

    public DiscoveryConfiguration getDiscoveryConfiguration() {
        return discoveryConfiguration;
    }

    public void setDiscoveryConfiguration(DiscoveryConfiguration discoveryConfiguration) {
        this.discoveryConfiguration = discoveryConfiguration;
    }

    public RelationMetadataAction getAction() {
        return action;
    }

    public void setAction(RelationMetadataAction action) {
        this.action = action;
    }

    public List<SecurityCheck> getSecurity() {
        return security;
    }

    public void setSecurity(List<SecurityCheck> security) {
        this.security = security;
    }

}
