package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Authorization;
import com.pmease.gitplex.core.manager.AuthorizationManager;

@Singleton
public class DefaultAuthorizationManager extends AbstractEntityDao<Authorization> implements AuthorizationManager {

	@Inject
	public DefaultAuthorizationManager(Dao dao) {
		super(dao);
	}

	@Override
	public Collection<Authorization> findAuthorizations(Account organization) {
		EntityCriteria<Authorization> criteria = EntityCriteria.of(Authorization.class);
		criteria.createCriteria("team").add(Restrictions.eq("organization", organization));
		return query(criteria);
	}

}
