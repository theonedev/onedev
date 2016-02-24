package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;

import com.pmease.commons.hibernate.dao.DefaultDao;
import com.pmease.gitplex.core.manager.AuthorizationManager;

@Singleton
public class DefaultAuthorizationManager extends DefaultDao implements AuthorizationManager {

	@Inject
	public DefaultAuthorizationManager(Provider<Session> sessionProvider) {
		super(sessionProvider);
	}

}
