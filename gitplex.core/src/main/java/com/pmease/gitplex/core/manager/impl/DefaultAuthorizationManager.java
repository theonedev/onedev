package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.Authorization;
import com.pmease.gitplex.core.manager.AuthorizationManager;

@Singleton
public class DefaultAuthorizationManager extends AbstractEntityDao<Authorization> implements AuthorizationManager {

	@Inject
	public DefaultAuthorizationManager(Dao dao) {
		super(dao);
	}

}
