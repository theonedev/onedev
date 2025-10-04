package io.onedev.server.service.impl;

import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;

import io.onedev.server.service.UserAuthorizationService;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.persistence.annotation.Transactional;

@Singleton
public class DefaultUserAuthorizationService extends BaseEntityService<UserAuthorization>
		implements UserAuthorizationService {

	@Transactional
	@Override
	public void syncAuthorizations(User user, Collection<UserAuthorization> authorizations) {
		for (var it = user.getProjectAuthorizations().iterator(); it.hasNext();) {
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
			for (var authorization: user.getProjectAuthorizations()) {
				if (authorization.getProject().equals(newAuthorization.getProject()) 
						&& authorization.getRole().equals(newAuthorization.getRole())) {
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
		for (var it = project.getUserAuthorizations().iterator(); it.hasNext();) {
			var authorization = it.next();
			var found = false;
			for (var newAuthorization: authorizations) {
				if (newAuthorization.getUser().equals(authorization.getUser()) 
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
			for (var authorization: project.getUserAuthorizations()) {
				if (authorization.getUser().equals(newAuthorization.getUser()) 
						&& authorization.getRole().equals(newAuthorization.getRole())) {
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
	public void createOrUpdate(UserAuthorization authorization) {
		dao.persist(authorization);
	}
	
}
