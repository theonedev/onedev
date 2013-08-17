package com.pmease.gitop.core.entitymanager.impl;

import java.util.Collection;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.persistence.Transactional;
import com.pmease.commons.persistence.dao.DefaultGenericDao;
import com.pmease.commons.persistence.dao.GeneralDao;
import com.pmease.gitop.core.entitymanager.RoleManager;
import com.pmease.gitop.core.model.Role;

@Singleton
public class DefaultRoleManager extends DefaultGenericDao<Role> implements RoleManager {

	public DefaultRoleManager(GeneralDao generalDao, Provider<Session> sessionProvider) {
		super(generalDao, sessionProvider);
	}

	@Transactional
	@Override
	public Collection<Role> getAnonymousRoles() {
		return search(new Criterion[]{Restrictions.eq("anonymous", true)});
	}

	@Transactional
	@Override
	public Collection<Role> getRegisterRoles() {
		return search(new Criterion[]{Restrictions.eq("register", true)});
	}

}
