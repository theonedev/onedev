package io.onedev.server.entitymanager.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.UserAuthorizationManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultUserAuthorizationManager extends BaseEntityManager<UserAuthorization> 
		implements UserAuthorizationManager {

	@Inject
	public DefaultUserAuthorizationManager(Dao dao) {
		super(dao);
	}
	
	@Transactional
	@Override
	public void syncAuthorizations(User user, Collection<UserAuthorization> authorizations) {
		for (Iterator<UserAuthorization> it = user.getProjectAuthorizations().iterator(); it.hasNext();) {
			UserAuthorization authorization = it.next();
			boolean found = false;
			for (UserAuthorization newAuthorization: authorizations) {
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

		for (UserAuthorization newAuthorization: authorizations) {
			boolean found = false;
			for (UserAuthorization authorization: user.getProjectAuthorizations()) {
				if (authorization.getProject().equals(newAuthorization.getProject())) {
					found = true;
					break;
				}
			}
			if (!found) {
				user.getProjectAuthorizations().add(newAuthorization);
				dao.persist(newAuthorization);
			}
		}
	}

	@Override
	public List<UserAuthorization> query() {
		return query(true);
	}

	@Override
	public int count() {
		return count(true);
	}
	
	@Transactional
	@Override
	public void syncAuthorizations(Project project, Collection<UserAuthorization> authorizations) {
		for (Iterator<UserAuthorization> it = project.getUserAuthorizations().iterator(); it.hasNext();) {
			UserAuthorization authorization = it.next();
			boolean found = false;
			for (UserAuthorization newAuthorization: authorizations) {
				if (newAuthorization.getUser().equals(authorization.getUser())) {
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

		for (UserAuthorization newAuthorization: authorizations) {
			boolean found = false;
			for (UserAuthorization authorization: project.getUserAuthorizations()) {
				if (authorization.getUser().equals(newAuthorization.getUser())) {
					found = true;
					break;
				}
			}
			if (!found) {
				project.getUserAuthorizations().add(newAuthorization);
				dao.persist(newAuthorization);
			}
		}
	}

	@Transactional
	@Override
	public void create(UserAuthorization authorization) {
		Preconditions.checkState(authorization.isNew());
		dao.persist(authorization);
	}

	@Transactional
	@Override
	public void update(UserAuthorization authorization) {
		Preconditions.checkState(!authorization.isNew());
		dao.persist(authorization);
	}
	
}
