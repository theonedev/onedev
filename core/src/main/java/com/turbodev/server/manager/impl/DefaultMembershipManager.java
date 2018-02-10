package com.turbodev.server.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.turbodev.server.manager.MembershipManager;
import com.turbodev.server.model.Membership;
import com.turbodev.server.persistence.annotation.Transactional;
import com.turbodev.server.persistence.dao.AbstractEntityManager;
import com.turbodev.server.persistence.dao.Dao;

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
