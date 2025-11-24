package io.onedev.server.service;

import java.util.Collection;

import io.onedev.server.model.ProjectEntitlement;
import io.onedev.server.model.User;

public interface ProjectEntitlementService extends EntityService<ProjectEntitlement> {

	void syncEntitlements(User ai, Collection<ProjectEntitlement> entitlements);
	
    void create(ProjectEntitlement entitlement);
	
}
