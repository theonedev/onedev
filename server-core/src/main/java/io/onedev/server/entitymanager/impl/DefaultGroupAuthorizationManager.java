package io.onedev.server.entitymanager.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.GroupAuthorizationManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultGroupAuthorizationManager extends BaseEntityManager<GroupAuthorization> 
		implements GroupAuthorizationManager {

	@Inject
	public DefaultGroupAuthorizationManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void syncAuthorizations(Group group, Collection<GroupAuthorization> authorizations) {
		for (Iterator<GroupAuthorization> it = group.getAuthorizations().iterator(); it.hasNext();) {
			GroupAuthorization authorization = it.next();
			boolean found = false;
			for (GroupAuthorization newAuthorization: authorizations) {
				if (newAuthorization.getProject().equals(authorization.getProject())) {
					found = true;
					authorization.setRole(newAuthorization.getRole());
					dao.persist(authorization);
				}
			}
			if (!found) {
				it.remove();
				delete(authorization);
			}
		}

		for (GroupAuthorization newAuthorization: authorizations) {
			boolean found = false;
			for (GroupAuthorization authorization: group.getAuthorizations()) {
				if (authorization.getProject().equals(newAuthorization.getProject())) {
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
		for (Iterator<GroupAuthorization> it = project.getGroupAuthorizations().iterator(); it.hasNext();) {
			GroupAuthorization authorization = it.next();
			boolean found = false;
			for (GroupAuthorization newAuthorization: authorizations) {
				if (newAuthorization.getGroup().equals(authorization.getGroup())) {
					found = true;
					authorization.setRole(newAuthorization.getRole());
					dao.persist(authorization);
				}
			}
			if (!found) {
				it.remove();
				delete(authorization);
			}
		}

		for (GroupAuthorization newAuthorization: authorizations) {
			boolean found = false;
			for (GroupAuthorization authorization: project.getGroupAuthorizations()) {
				if (authorization.getGroup().equals(newAuthorization.getGroup())) {
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
	public void create(GroupAuthorization authorization) {
		Preconditions.checkState(authorization.isNew());
		dao.persist(authorization);
	}

	@Transactional
	@Override
	public void update(GroupAuthorization authorization) {
		Preconditions.checkState(!authorization.isNew());
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
