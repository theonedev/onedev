package com.gitplex.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.gitplex.server.manager.TeamManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.Team;
import com.gitplex.server.model.support.BranchProtection;
import com.gitplex.server.model.support.TagProtection;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;

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
				for (BranchProtection protection: depot.getBranchProtections()) 
					protection.onTeamRename(depot, oldName, team.getName());
				for (TagProtection protection: depot.getTagProtections())
					protection.onTeamRename(oldName, team.getName());
			}
		}
		dao.persist(team);
	}

	@Transactional
	@Override
	public void delete(Team team) {
		for (Depot depot: team.getOrganization().getDepots()) {
			for (BranchProtection protection: depot.getBranchProtections()) 
				protection.onTeamDelete(depot, team.getName());
			for (TagProtection protection: depot.getTagProtections())
				protection.onTeamDelete(team.getName());
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
