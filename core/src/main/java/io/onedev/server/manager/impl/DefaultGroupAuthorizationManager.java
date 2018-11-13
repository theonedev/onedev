package io.onedev.server.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.manager.GroupAuthorizationManager;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

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
