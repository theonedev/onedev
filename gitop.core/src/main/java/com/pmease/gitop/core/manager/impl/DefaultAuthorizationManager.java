package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;

import com.pmease.commons.persistence.dao.DefaultGenericDao;
import com.pmease.commons.persistence.dao.GeneralDao;
import com.pmease.gitop.core.manager.AuthorizationManager;
import com.pmease.gitop.core.model.Authorization;

@Singleton
public class DefaultAuthorizationManager extends DefaultGenericDao<Authorization> implements AuthorizationManager {

	@Inject
	public DefaultAuthorizationManager(GeneralDao generalDao, Provider<Session> sessionProvider) {
		super(generalDao, sessionProvider);
	}

}
