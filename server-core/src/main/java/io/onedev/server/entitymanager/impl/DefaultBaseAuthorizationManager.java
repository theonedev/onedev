package io.onedev.server.entitymanager.impl;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.onedev.server.entitymanager.BaseAuthorizationManager;
import io.onedev.server.model.BaseAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.model.Role;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultBaseAuthorizationManager extends BaseEntityManager<BaseAuthorization> 
		implements BaseAuthorizationManager {

	@Inject
	public DefaultBaseAuthorizationManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void syncRoles(Project project, Collection<Role> roles) {
		for (var it = project.getBaseAuthorizations().iterator(); it.hasNext();) {
			var authorization = it.next();
			var found = false;
			for (var newRole: roles) {
				if (newRole.equals(authorization.getRole())) {
					found = true;
					break;
				}
			}
			if (!found) {
				it.remove();
				delete(authorization);
			}
		}

		for (var newRole: roles) {
			var found = false;
			for (var authorization: project.getBaseAuthorizations()) {
				if (authorization.getRole().equals(newRole)) {
					found = true;
					break;
				}
			}
			if (!found) {
				var authorization = new BaseAuthorization();
				authorization.setProject(project);
				authorization.setRole(newRole);
				project.getBaseAuthorizations().add(authorization);
				dao.persist(authorization);
			}
		}
	}
	
	@Transactional
	@Override
	public void create(BaseAuthorization authorization) {
		Preconditions.checkArgument(authorization.isNew());
		dao.persist(authorization);
	}

	@Override
	public List<BaseAuthorization> query() {
		return query(true);
	}

	@Override
	public int count() {
		return count(true);
	}
	
}
