package com.pmease.gitop.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Transactional;
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

	@Transactional
	@Override
	public Collection<Role> findAnonymousRoles() {
		return query(new Criterion[]{Restrictions.eq("anonymous", true)});
	}

	@Transactional
	@Override
	public Collection<Role> findRegisterRoles() {
		return query(new Criterion[]{Restrictions.eq("register", true)});
	}

}
