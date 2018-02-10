package com.turbodev.server.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.turbodev.server.manager.GroupAuthorizationManager;
import com.turbodev.server.model.GroupAuthorization;
import com.turbodev.server.persistence.annotation.Transactional;
import com.turbodev.server.persistence.dao.AbstractEntityManager;
import com.turbodev.server.persistence.dao.Dao;

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
