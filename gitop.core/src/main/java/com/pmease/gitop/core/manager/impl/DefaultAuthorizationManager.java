package com.pmease.gitop.core.manager.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.AuthorizationManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.Authorization;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.model.permission.operation.GeneralOperation;

@Singleton
public class DefaultAuthorizationManager extends AbstractGenericDao<Authorization> 
		implements AuthorizationManager {

	private final UserManager userManager;
	
	@Inject
	public DefaultAuthorizationManager(GeneralDao generalDao, UserManager userManager) {
		super(generalDao);
		this.userManager = userManager;
	}

	public Collection<User> listAuthorizedUsers(Project project, GeneralOperation operation) {
		Set<User> authorizedUsers = new HashSet<User>();
		for (User user: userManager.query()) {
			if (user.asSubject().isPermitted(new ObjectPermission(project, operation)))
				authorizedUsers.add(user);
		}
		return authorizedUsers;
	}
	
}
