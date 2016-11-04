package com.gitplex.server.core.manager.impl;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.gitplex.commons.hibernate.Sessional;
import com.gitplex.commons.hibernate.Transactional;
import com.gitplex.commons.hibernate.dao.AbstractEntityManager;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.commons.hibernate.dao.EntityCriteria;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.Depot;
import com.gitplex.server.core.entity.Team;
import com.gitplex.server.core.gatekeeper.GateKeeper;
import com.gitplex.server.core.manager.TeamManager;

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
				for (GateKeeper gateKeeper: depot.getGateKeepers()) {
					gateKeeper.onTeamRename(oldName, team.getName());
				}
			}
		}
		dao.persist(team);
	}

	@Transactional
	@Override
	public void delete(Team team) {
		for (Depot depot: team.getOrganization().getDepots()) {
			for (Iterator<GateKeeper> it = depot.getGateKeepers().iterator(); it.hasNext();) {
				if (it.next().onTeamDelete(team.getName()))
					it.remove();
			}
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
