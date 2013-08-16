package com.pmease.gitop.core.entitymanager.impl;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;

import com.pmease.commons.persistence.Transactional;
import com.pmease.commons.persistence.dao.DefaultGenericDao;
import com.pmease.commons.persistence.dao.GeneralDao;
import com.pmease.gitop.core.entitymanager.GroupManager;
import com.pmease.gitop.core.entitymanager.UserManager;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.TeamMembership;

@Singleton
public class DefaultGroupManager extends DefaultGenericDao<Team> implements GroupManager {

	private final UserManager userManager;
	
	public DefaultGroupManager(GeneralDao generalDao, Provider<Session> sessionProvider, UserManager userManager) {
		super(generalDao, sessionProvider);
		this.userManager = userManager;
	}

	@Transactional
	@Override
	public Collection<Team> getGroups(Long userId) {
		Collection<Team> groups = new ArrayList<Team>();
		for (TeamMembership membership: userManager.load(userId).getMemberships())
			groups.add(membership.getGroup());
		
		return groups;
	}

}
