package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Membership;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.MembershipManager;

public class DefaultMembershipManager extends AbstractEntityDao<Membership> implements MembershipManager {

	private final AccountManager accountManager;
	
	@Inject
	public DefaultMembershipManager(Dao dao, AccountManager accountManager) {
		super(dao);
		this.accountManager = accountManager;
	}

	@Transactional
	@Override
	public void save(Membership membership) {
		if (membership.getOrganization().isNew())
			accountManager.save(membership.getOrganization(), null);
		persist(membership);
		
		/* 
		 * below statement makes sure that organization is not updated
		 * while we edit the membership to add users to organization 
		 * teams 
		 */
		persist(membership.getOrganization());
	}

	@Transactional
	@Override
	public void delete(Set<Membership> memberships) {
		for (Membership membership: memberships)
			remove(membership);
	}

	@Transactional
	@Override
	public void save(Collection<Membership> memberships) {
		for (Membership membership: memberships)
			persist(membership);
	}

	@Override
	public Membership find(Account organization, Account user) {
		EntityCriteria<Membership> criteria = newCriteria();
		criteria.add(Restrictions.eq("organization", organization)).add(Restrictions.eq("user", user));
		return find(criteria);
	}

}
