package io.onedev.server.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.manager.MembershipManager;
import io.onedev.server.model.Membership;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

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
