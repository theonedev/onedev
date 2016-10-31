package com.gitplex.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.TeamMembership;
import com.gitplex.core.manager.TeamMembershipManager;
import com.gitplex.commons.hibernate.Sessional;
import com.gitplex.commons.hibernate.Transactional;
import com.gitplex.commons.hibernate.dao.AbstractEntityManager;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.commons.hibernate.dao.EntityCriteria;

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
