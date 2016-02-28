package com.pmease.gitplex.core.manager.impl;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.Team;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.TeamManager;

@Singleton
public class DefaultTeamManager extends AbstractEntityDao<Team> implements TeamManager {

	private final AccountManager userManager;
	
	@Inject
	public DefaultTeamManager(Dao dao, AccountManager userManager) {
		super(dao);
		
		this.userManager = userManager;
	}

	@Sessional
	@Override
	public Team findBy(Account owner, String teamName) {
		return find(EntityCriteria.of(Team.class)
				.add(Restrictions.eq("owner", owner))
				.add(Restrictions.eq("name", teamName)));
	}
	
	@Sessional
	@Override
	public Team findBy(String teamFQN) {
    	String userName = StringUtils.substringBefore(teamFQN, Depot.FQN_SEPARATOR);
    	Account user = userManager.findByName(userName);
    	if (user != null)
    		return findBy(user, StringUtils.substringAfter(teamFQN, Depot.FQN_SEPARATOR));
    	else
    		return null;
	}

	@Transactional
	@Override
	public void delete(Team team) {
		for (Depot each: team.getOwner().getDepots()) {
			for (Iterator<GateKeeper> it = each.getGateKeepers().iterator(); it.hasNext();) {
				if (it.next().onTeamDelete(team))
					it.remove();
			}
		}
	}

	@Transactional
	@Override
	public void rename(Account teamOwner, String oldName, String newName) {
		Query query = getSession().createQuery("update Team set name=:newName where "
				+ "owner=:owner and name=:oldName");
		query.setParameter("owner", teamOwner);
		query.setParameter("oldName", oldName);
		query.setParameter("newName", newName);
		query.executeUpdate();
		
		for (Depot depot: teamOwner.getDepots()) {
			for (GateKeeper gateKeeper: depot.getGateKeepers()) {
				gateKeeper.onTeamRename(oldName, newName);
			}
		}
	}

}
