package io.onedev.server.service.impl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.onedev.server.model.GroupEntitlement;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.GroupEntitlementService;

@Singleton
public class DefaultGroupEntitlementService extends BaseEntityService<GroupEntitlement> 
		implements GroupEntitlementService {

	@Override
	public List<GroupEntitlement> query() {
		return query(true);
	}
	
	@Override
	public int count() {
		return count(true);
	}

	@Transactional
	@Override
	public void syncEntitlements(User ai, Collection<GroupEntitlement> entitlements) {
		var newGroups = entitlements.stream()
				.map(GroupEntitlement::getGroup)
				.collect(Collectors.toSet());
		
		ai.getGroupEntitlements().removeIf(it -> {
			if (!newGroups.contains(it.getGroup())) {
				delete(it);
				return true;
			}
			return false;
		});
		
		var existingGroups = ai.getGroupEntitlements().stream()
				.map(GroupEntitlement::getGroup)
				.collect(Collectors.toSet());
		entitlements.stream()
				.filter(it -> !existingGroups.contains(it.getGroup()))
				.forEach(it -> {
					ai.getGroupEntitlements().add(it);
					dao.persist(it);
				});
	}

	@Transactional
	@Override
	public void create(GroupEntitlement entitlement) {
		Preconditions.checkState(entitlement.isNew());
		dao.persist(entitlement);
	}

}
