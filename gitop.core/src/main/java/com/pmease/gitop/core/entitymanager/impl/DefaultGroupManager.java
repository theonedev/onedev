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
import com.pmease.gitop.core.model.Group;
import com.pmease.gitop.core.model.Membership;

@Singleton
public class DefaultGroupManager extends DefaultGenericDao<Group> implements GroupManager {

	private final UserManager userManager;
	
	public DefaultGroupManager(GeneralDao generalDao, Provider<Session> sessionProvider, UserManager userManager) {
		super(generalDao, sessionProvider);
		this.userManager = userManager;
	}

	@Transactional
	@Override
	public Collection<Group> getGroups(Long userId) {
		Collection<Group> groups = new ArrayList<Group>();
		for (Membership membership: userManager.load(userId).getMemberships())
			groups.add(membership.getGroup());
		
		return groups;
	}

}
