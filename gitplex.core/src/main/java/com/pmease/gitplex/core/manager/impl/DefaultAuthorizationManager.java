package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.SecurityUtils;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.Comment;
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.model.Vote;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.core.permission.operation.GeneralOperation;

@Singleton
public class DefaultAuthorizationManager implements AuthorizationManager {

	private final UserManager userManager;
	
	private final Dao dao;
	
	@Inject
	public DefaultAuthorizationManager(Dao dao, UserManager userManager) {
		this.dao = dao;
		this.userManager = userManager;
	}

	public Collection<User> listAuthorizedUsers(Repository repository, GeneralOperation operation) {
		Set<User> authorizedUsers = new HashSet<User>();
		for (User user: dao.query(EntityCriteria.of(User.class), 0, 0)) {
			if (user.asSubject().isPermitted(new ObjectPermission(repository, operation)))
				authorizedUsers.add(user);
		}
		return authorizedUsers;
	}

	@Override
	public boolean canModify(PullRequest request) {
		Repository repository = request.getTarget().getRepository();
		if (SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryAdmin(repository))) {
			return true;
		} else {
			User currentUser = userManager.getCurrent();
			User submitter = request.getSubmitter();
			return submitter != null && submitter.equals(currentUser);
		}
	}
	
	@Override
	public boolean canModify(Vote vote) {
		Repository repository = vote.getUpdate().getRequest().getTarget().getRepository();
		if (SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryAdmin(repository))) {
			return true;
		} else {
			return vote.getVoter().equals(userManager.getCurrent());
		}
	}

	@Override
	public boolean canModify(Comment comment) {
		User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
		if (currentUser == null) {
			return false;
		} else {
			if (currentUser.equals(comment.getUser())) {
				return true;
			} else {
				ObjectPermission adminPermission = ObjectPermission.ofRepositoryAdmin(comment.getRepository());
				return SecurityUtils.getSubject().isPermitted(adminPermission);
			}
		}
	}

}
