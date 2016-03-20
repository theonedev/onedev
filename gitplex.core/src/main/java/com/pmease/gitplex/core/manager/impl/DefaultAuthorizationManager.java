package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Authorization;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.listener.DepotListener;
import com.pmease.gitplex.core.manager.AuthorizationManager;

@Singleton
public class DefaultAuthorizationManager extends AbstractEntityDao<Authorization> 
		implements AuthorizationManager, DepotListener {

	@Inject
	public DefaultAuthorizationManager(Dao dao) {
		super(dao);
	}

	@Override
	public void onDepotDelete(Depot depot) {
	}

	@Override
	public void onDepotRename(Depot renamedDepot, String oldName) {
	}

	@Transactional
	@Override
	public void onDepotTransfer(Depot depot, Account oldAccount) {
		Query query = getSession().createQuery("delete from Authorization where depot=:depot");
		query.setParameter("depot", depot);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void delete(Collection<Authorization> authorizations) {
		for (Authorization authorization: authorizations)
			remove(authorization);
	}

	@Transactional
	@Override
	public Collection<Authorization> query(Account organization) {
		EntityCriteria<Authorization> criteria = newCriteria(); 
		criteria.createCriteria("depot").add(Restrictions.eq("account", organization));
		return query(criteria);
	}

}
