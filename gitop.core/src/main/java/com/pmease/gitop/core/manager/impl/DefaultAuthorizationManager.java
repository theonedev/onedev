package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.dao.DefaultGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.AuthorizationManager;
import com.pmease.gitop.core.model.Authorization;

@Singleton
public class DefaultAuthorizationManager extends DefaultGenericDao<Authorization> implements AuthorizationManager {

	@Inject
	public DefaultAuthorizationManager(GeneralDao generalDao) {
		super(generalDao);
	}

}
