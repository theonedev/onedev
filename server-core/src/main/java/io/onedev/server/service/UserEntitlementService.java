package io.onedev.server.service;

import java.util.Collection;

import io.onedev.server.model.User;
import io.onedev.server.model.UserEntitlement;

public interface UserEntitlementService extends EntityService<UserEntitlement> {

	void syncEntitlements(User ai, Collection<UserEntitlement> entitlements);
	
    void create(UserEntitlement entitlement);
	
}
