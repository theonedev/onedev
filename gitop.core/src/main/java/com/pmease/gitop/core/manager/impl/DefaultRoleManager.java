package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;

import com.pmease.commons.hibernate.dao.DefaultGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.RoleManager;
import com.pmease.gitop.core.model.Role;

@Singleton
public class DefaultRoleManager extends DefaultGenericDao<Role> implements RoleManager {

	@Inject
	public DefaultRoleManager(GeneralDao generalDao, Provider<Session> sessionProvider) {
		super(generalDao);
	}

}
