package io.onedev.server.service.impl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.onedev.server.model.ProjectEntitlement;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.ProjectEntitlementService;

@Singleton
public class DefaultProjectEntitlementService extends BaseEntityService<ProjectEntitlement> 
		implements ProjectEntitlementService {

	@Override
	public List<ProjectEntitlement> query() {
		return query(true);
	}
	
	@Override
	public int count() {
		return count(true);
	}

	@Transactional
	@Override
	public void syncEntitlements(User ai, Collection<ProjectEntitlement> entitlements) {
		var newProjects = entitlements.stream()
				.map(ProjectEntitlement::getProject)
				.collect(Collectors.toSet());
		
		ai.getProjectEntitlements().removeIf(it -> {
			if (!newProjects.contains(it.getProject())) {
				delete(it);
				return true;
			}
			return false;
		});
		
		var existingProjects = ai.getProjectEntitlements().stream()
				.map(ProjectEntitlement::getProject)
				.collect(Collectors.toSet());
		entitlements.stream()
				.filter(it -> !existingProjects.contains(it.getProject()))
				.forEach(it -> {
					ai.getProjectEntitlements().add(it);
					dao.persist(it);
				});
	}

	@Transactional
	@Override
	public void create(ProjectEntitlement entitlement) {
		Preconditions.checkState(entitlement.isNew());
		dao.persist(entitlement);
	}

}
