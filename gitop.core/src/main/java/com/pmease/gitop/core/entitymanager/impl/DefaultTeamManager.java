package com.pmease.gitop.core.entitymanager.impl;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;

import com.pmease.commons.persistence.Transactional;
import com.pmease.commons.persistence.dao.DefaultGenericDao;
import com.pmease.commons.persistence.dao.GeneralDao;
import com.pmease.gitop.core.entitymanager.TeamManager;
import com.pmease.gitop.core.entitymanager.AccountManager;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.Membership;

@Singleton
public class DefaultTeamManager extends DefaultGenericDao<Team> implements TeamManager {

	private final AccountManager accountManager;
	
	public DefaultTeamManager(GeneralDao generalDao, Provider<Session> sessionProvider, AccountManager accountManager) {
		super(generalDao, sessionProvider);
		this.accountManager = accountManager;
	}

	@Transactional
	@Override
	public Collection<Team> getTeams(Long accountId) {
		Collection<Team> teams = new ArrayList<Team>();
		for (Membership membership: accountManager.load(accountId).getMemberships())
			teams.add(membership.getTeam());
		
		return teams;
	}

}
