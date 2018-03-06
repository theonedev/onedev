package io.onedev.server.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.manager.UserAuthorizationManager;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultUserAuthorizationManager extends AbstractEntityManager<UserAuthorization> 
		implements UserAuthorizationManager {

	@Inject
	public DefaultUserAuthorizationManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void delete(Collection<UserAuthorization> authorizations) {
		for (UserAuthorization authorization: authorizations)
			dao.remove(authorization);
	}
	
}
