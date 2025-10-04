package io.onedev.server.service.impl;

import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;

import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.GroupAuthorizationService;

@Singleton
public class DefaultGroupAuthorizationService extends BaseEntityService<GroupAuthorization>
		implements GroupAuthorizationService {

	@Transactional
	@Override
	public void syncAuthorizations(Group group, Collection<GroupAuthorization> authorizations) {
		for (var it = group.getAuthorizations().iterator(); it.hasNext();) {
			var authorization = it.next();
			var found = false;
			for (var newAuthorization: authorizations) {
				if (newAuthorization.getProject().equals(authorization.getProject()) 
						&& newAuthorization.getRole().equals(authorization.getRole())) {
					found = true;
					break;
				}
			}
			if (!found) {
				it.remove();
				delete(authorization);
			}
		}

		for (var newAuthorization: authorizations) {
			var found = false;
			for (var authorization: group.getAuthorizations()) {
				if (authorization.getProject().equals(newAuthorization.getProject()) 
						&& authorization.getRole().equals(newAuthorization.getRole())) {
					found = true;
					break;
				}
			}
			if (!found) {
				group.getAuthorizations().add(newAuthorization);
				dao.persist(newAuthorization);
			}
		}
	}

	@Transactional
	@Override
	public void syncAuthorizations(Project project, Collection<GroupAuthorization> authorizations) {
		for (var it = project.getGroupAuthorizations().iterator(); it.hasNext();) {
			var authorization = it.next();
			var found = false;
			for (var newAuthorization: authorizations) {
				if (newAuthorization.getGroup().equals(authorization.getGroup()) 
						&& newAuthorization.getRole().equals(authorization.getRole())) {
					found = true;
					break;
				}
			}
			if (!found) {
				it.remove();
				delete(authorization);
			}
		}

		for (var newAuthorization: authorizations) {
			var found = false;
			for (var authorization: project.getGroupAuthorizations()) {
				if (authorization.getGroup().equals(newAuthorization.getGroup()) 
						&& authorization.getRole().equals(newAuthorization.getRole())) {
					found = true;
					break;
				}
			}
			if (!found) {
				project.getGroupAuthorizations().add(newAuthorization);
				dao.persist(newAuthorization);
			}
		}
	}

	@Transactional
	@Override
	public void createOrUpdate(GroupAuthorization authorization) {
		dao.persist(authorization);
	}
	
	@Override
	public List<GroupAuthorization> query() {
		return query(true);
	}

	@Override
	public int count() {
		return count(true);
	}
	
}
