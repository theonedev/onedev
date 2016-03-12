package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
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
	}

}
