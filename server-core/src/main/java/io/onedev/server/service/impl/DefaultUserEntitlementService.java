package io.onedev.server.service.impl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.onedev.server.model.User;
import io.onedev.server.model.UserEntitlement;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.UserEntitlementService;

@Singleton
public class DefaultUserEntitlementService extends BaseEntityService<UserEntitlement> 
		implements UserEntitlementService {

	@Override
	public List<UserEntitlement> query() {
		return query(true);
	}
	
	@Override
	public int count() {
		return count(true);
	}

	@Transactional
	@Override
	public void syncEntitlements(User ai, Collection<UserEntitlement> entitlements) {
		var newUsers = entitlements.stream()
				.map(UserEntitlement::getUser)
				.collect(Collectors.toSet());
		
		ai.getUserEntitlements().removeIf(it -> {
			if (!newUsers.contains(it.getUser())) {
				delete(it);
				return true;
			}
			return false;
		});
		
		var existingUsers = ai.getUserEntitlements().stream()
				.map(UserEntitlement::getUser)
				.collect(Collectors.toSet());
		entitlements.stream()
				.filter(it -> !existingUsers.contains(it.getUser()))
				.forEach(it -> {
					ai.getUserEntitlements().add(it);
					dao.persist(it);
				});
	}

	@Transactional
	@Override
	public void create(UserEntitlement entitlement) {
		Preconditions.checkState(entitlement.isNew());
		dao.persist(entitlement);
	}

}
