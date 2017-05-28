package com.gitplex.server.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.gitplex.server.manager.MembershipManager;
import com.gitplex.server.model.Membership;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;

@Singleton
public class DefaultMembershipManager extends AbstractEntityManager<Membership> implements MembershipManager {

	@Inject
	public DefaultMembershipManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void delete(Collection<Membership> memberships) {
		for (Membership membership: memberships)
			dao.remove(membership);
	}

}
