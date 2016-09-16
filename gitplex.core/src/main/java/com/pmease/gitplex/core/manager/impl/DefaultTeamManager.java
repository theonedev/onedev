package com.pmease.gitplex.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.Team;
import com.pmease.gitplex.core.gatekeeper.DefaultGateKeeper;
import com.pmease.gitplex.core.manager.TeamManager;

@Singleton
public class DefaultTeamManager extends AbstractEntityManager<Team> implements TeamManager {

	@Inject
	public DefaultTeamManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void save(Team team, String oldName) {
		if (oldName != null && !oldName.equals(team.getName())) {
			for (Depot depot: team.getOrganization().getDepots()) {
				depot.getGateKeeper().onTeamRename(oldName, team.getName());
			}
		}
		dao.persist(team);
	}

	@Transactional
	@Override
	public void delete(Team team) {
		for (Depot depot: team.getOrganization().getDepots()) {
			if (depot.getGateKeeper().onTeamDelete(team.getName()))
				depot.setGateKeeper(new DefaultGateKeeper());
		}
		dao.remove(team);
	}

	@Sessional
	@Override
	public Team find(Account organization, String name) {
		EntityCriteria<Team> criteria = newCriteria();
		criteria.add(Restrictions.eq("organization", organization)).add(Restrictions.eq("name", name));
		return find(criteria);
	}
	
}
