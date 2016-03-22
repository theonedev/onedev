package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.UserAuthorization;
import com.pmease.gitplex.core.manager.UserAuthorizationManager;

@Singleton
public class DefaultUserAuthorizationManager extends AbstractEntityDao<UserAuthorization> 
		implements UserAuthorizationManager {

	@Inject
	public DefaultUserAuthorizationManager(Dao dao) {
		super(dao);
	}

	@Override
	public UserAuthorization find(Account user, Depot depot) {
		EntityCriteria<UserAuthorization> criteria = newCriteria();
		criteria.add(Restrictions.eq("user", user)).add(Restrictions.eq("depot", depot));
		return find(criteria);
	}

	@Override
	public Collection<UserAuthorization> query(Account organization) {
		EntityCriteria<UserAuthorization> criteria = newCriteria();
		criteria.createCriteria("depot").add(Restrictions.eq("account", organization));
		return query(criteria);
	}

}
