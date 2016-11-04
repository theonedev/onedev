package com.gitplex.server.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.gitplex.commons.hibernate.Sessional;
import com.gitplex.commons.hibernate.Transactional;
import com.gitplex.commons.hibernate.dao.AbstractEntityManager;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.commons.hibernate.dao.EntityCriteria;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.Depot;
import com.gitplex.server.core.entity.UserAuthorization;
import com.gitplex.server.core.manager.UserAuthorizationManager;
import com.gitplex.server.core.security.privilege.DepotPrivilege;

@Singleton
public class DefaultUserAuthorizationManager extends AbstractEntityManager<UserAuthorization> 
		implements UserAuthorizationManager {

	@Inject
	public DefaultUserAuthorizationManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void save(UserAuthorization entity) {
		DepotPrivilege privilege = entity.getPrivilege();
		
		/*
		 * Admin privilege is not allowed for team to make permission management consistent. 
		 * That is: the person able to administer a depot can also administer the whole 
		 * account, so that they can edit authorizations either from depot side, or from 
		 * team side 
		 */
		Preconditions.checkArgument(privilege == DepotPrivilege.READ || privilege == DepotPrivilege.WRITE);
		
		dao.persist(entity);
	}
	
	@Sessional
	@Override
	public UserAuthorization find(Account user, Depot depot) {
		EntityCriteria<UserAuthorization> criteria = newCriteria();
		criteria.add(Restrictions.eq("user", user)).add(Restrictions.eq("depot", depot));
		return find(criteria);
	}

	@Sessional
	@Override
	public Collection<UserAuthorization> findAll(Account account) {
		EntityCriteria<UserAuthorization> criteria = newCriteria();
		criteria.createCriteria("depot").add(Restrictions.eq("account", account));
		return findAll(criteria);
	}

	@Transactional
	@Override
	public void delete(Collection<UserAuthorization> authorizations) {
		for (UserAuthorization authorization: authorizations)
			dao.remove(authorization);
	}
	
}
