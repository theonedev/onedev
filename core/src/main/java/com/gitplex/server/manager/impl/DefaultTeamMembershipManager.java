package com.gitplex.server.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.gitplex.server.manager.TeamMembershipManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.TeamMembership;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultTeamMembershipManager extends AbstractEntityManager<TeamMembership> 
		implements TeamMembershipManager {

	@Inject
	public DefaultTeamMembershipManager(Dao dao) {
		super(dao);
	}

	@Sessional
	@Override
	public Collection<TeamMembership> findAll(Account organization, Account user) {
		EntityCriteria<TeamMembership> criteria = newCriteria();
		criteria.add(Restrictions.eq("user", user));
		criteria.createCriteria("team").add(Restrictions.eq("organization", organization));
		return findAll(criteria);
	}

	@Sessional
	@Override
	public Collection<TeamMembership> findAll(Account organization) {
		EntityCriteria<TeamMembership> criteria = newCriteria();
		criteria.createCriteria("team").add(Restrictions.eq("organization", organization));
		return findAll(criteria);
	}

	@Transactional
	@Override
	public void delete(Collection<TeamMembership> memberships) {
		for (TeamMembership membership: memberships)
			dao.remove(membership);
	}

}
