package com.pmease.gitop.core.entitymanager.impl;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;

import com.pmease.commons.persistence.Transactional;
import com.pmease.commons.persistence.dao.DefaultGenericDao;
import com.pmease.commons.persistence.dao.GeneralDao;
import com.pmease.gitop.core.entitymanager.RoleManager;
import com.pmease.gitop.core.entitymanager.AccountManager;
import com.pmease.gitop.core.model.Authorization;
import com.pmease.gitop.core.model.Role;

@Singleton
public class DefaultRoleManager extends DefaultGenericDao<Role> implements RoleManager {

	private final AccountManager accountManager;
	
	public DefaultRoleManager(GeneralDao generalDao, Provider<Session> sessionProvider, AccountManager accountManager) {
		super(generalDao, sessionProvider);
		this.accountManager = accountManager;
	}

	@Transactional
	@Override
	public Collection<Role> getRoles(Long accountId) {
		Collection<Role> roles = new ArrayList<Role>();
		for (Authorization authorization: accountManager.load(accountId).getAuthorizations())
			roles.add(authorization.getRole());
		
		return roles;
	}

}
