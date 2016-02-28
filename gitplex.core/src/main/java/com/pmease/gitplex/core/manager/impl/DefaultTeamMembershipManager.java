package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;

import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.TeamMembership;
import com.pmease.gitplex.core.manager.TeamMembershipManager;

public class DefaultTeamMembershipManager extends AbstractEntityDao<TeamMembership> 
		implements TeamMembershipManager {

	@Inject
	public DefaultTeamMembershipManager(Dao dao) {
		super(dao);
	}

}
