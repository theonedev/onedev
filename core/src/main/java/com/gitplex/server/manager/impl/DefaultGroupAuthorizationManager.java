package com.gitplex.server.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.server.manager.GroupAuthorizationManager;
import com.gitplex.server.model.GroupAuthorization;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;

@Singleton
public class DefaultGroupAuthorizationManager extends AbstractEntityManager<GroupAuthorization> 
		implements GroupAuthorizationManager {

	@Inject
	public DefaultGroupAuthorizationManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void delete(Collection<GroupAuthorization> authorizations) {
		for (GroupAuthorization authorization: authorizations)
			delete(authorization);
	}

}
