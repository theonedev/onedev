package com.gitplex.server.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.server.manager.UserAuthorizationManager;
import com.gitplex.server.model.UserAuthorization;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;

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
