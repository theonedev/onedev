package io.onedev.server.service;

import java.util.Collection;

import io.onedev.server.model.GroupEntitlement;
import io.onedev.server.model.User;

public interface GroupEntitlementService extends EntityService<GroupEntitlement> {

	void syncEntitlements(User ai, Collection<GroupEntitlement> entitlements);
	
    void create(GroupEntitlement entitlement);
	
}
