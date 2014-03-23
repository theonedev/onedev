package com.pmease.gitop.core.manager.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.SecurityUtils;

import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.AuthorizationManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.Authorization;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
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

	public Collection<User> listAuthorizedUsers(Repository project, GeneralOperation operation) {
		Set<User> authorizedUsers = new HashSet<User>();
		for (User user: userManager.query()) {
			if (user.asSubject().isPermitted(new ObjectPermission(project, operation)))
				authorizedUsers.add(user);
		}
		return authorizedUsers;
	}

	@Override
	public boolean canModify(PullRequest request) {
		Repository project = request.getTarget().getProject();
		if (SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectAdmin(project))) {
			return true;
		} else {
			User currentUser = userManager.getCurrent();
			User submittedBy = request.getSubmittedBy();
			return submittedBy != null && submittedBy.equals(currentUser);
		}
	}
	
	@Override
	public boolean canModify(Vote vote) {
		Repository project = vote.getUpdate().getRequest().getTarget().getProject();
		if (SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectAdmin(project))) {
			return true;
		} else {
			return vote.getVoter().equals(userManager.getCurrent());
		}
	}

}
