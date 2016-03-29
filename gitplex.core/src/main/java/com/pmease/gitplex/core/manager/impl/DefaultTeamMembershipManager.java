package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.TeamMembership;
import com.pmease.gitplex.core.manager.TeamMembershipManager;

@Singleton
public class DefaultTeamMembershipManager extends AbstractEntityDao<TeamMembership> 
		implements TeamMembershipManager {

	@Inject
	public DefaultTeamMembershipManager(Dao dao) {
		super(dao);
	}

	@Sessional
	@Override
	public Collection<TeamMembership> query(Account organization, Account user) {
		EntityCriteria<TeamMembership> criteria = newCriteria();
		criteria.add(Restrictions.eq("user", user));
		criteria.createCriteria("team").add(Restrictions.eq("organization", organization));
		return query(criteria);
	}

	@Sessional
	@Override
	public Collection<TeamMembership> query(Account organization) {
		EntityCriteria<TeamMembership> criteria = newCriteria();
		criteria.createCriteria("team").add(Restrictions.eq("organization", organization));
		return query(criteria);
	}

	@Transactional
	@Override
	public void delete(Collection<TeamMembership> memberships) {
		for (TeamMembership membership: memberships)
			remove(membership);
	}

}
