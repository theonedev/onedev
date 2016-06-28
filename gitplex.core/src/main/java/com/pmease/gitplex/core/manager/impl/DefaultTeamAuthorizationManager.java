package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.TeamAuthorization;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.listener.DepotListener;
import com.pmease.gitplex.core.manager.TeamAuthorizationManager;
import com.pmease.gitplex.core.security.privilege.DepotPrivilege;

@Singleton
public class DefaultTeamAuthorizationManager extends AbstractEntityManager<TeamAuthorization> 
		implements TeamAuthorizationManager, DepotListener {

	@Inject
	public DefaultTeamAuthorizationManager(Dao dao) {
		super(dao);
	}

	@Override
	public void onDeleteDepot(Depot depot) {
	}

	@Override
	public void onRenameDepot(Depot renamedDepot, String oldName) {
	}

	@Transactional
	@Override
	public void save(TeamAuthorization entity) {
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

	@Transactional
	@Override
	public void onTransferDepot(Depot depot, Account oldAccount) {
		Query query = getSession().createQuery("delete from TeamAuthorization where depot=:depot");
		query.setParameter("depot", depot);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void delete(Collection<TeamAuthorization> authorizations) {
		for (TeamAuthorization authorization: authorizations)
			dao.remove(authorization);
	}

	@Sessional
	@Override
	public Collection<TeamAuthorization> query(Account organization) {
		EntityCriteria<TeamAuthorization> criteria = newCriteria(); 
		criteria.createCriteria("depot").add(Restrictions.eq("account", organization));
		return query(criteria);
	}

	@Override
	public void onSaveDepot(Depot depot) {
	}

}
