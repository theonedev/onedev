package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.SecurityUtils;

import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.Comment;
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.permission.Permission;
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
			if (user.asSubject().isPermitted(new Permission(repository, operation)))
				authorizedUsers.add(user);
		}
		return authorizedUsers;
	}

	@Override
	public boolean canModifyRequest(PullRequest request) {
		Repository repository = request.getTarget().getRepository();
		if (SecurityUtils.getSubject().isPermitted(Permission.ofRepositoryAdmin(repository))) {
			return true;
		} else {
			User currentUser = userManager.getCurrent();
			User submitter = request.getSubmitter();
			return submitter.equals(currentUser);
		}
	}
	
	@Override
	public boolean canModifyReview(Review review) {
		Repository repository = review.getUpdate().getRequest().getTarget().getRepository();
		if (SecurityUtils.getSubject().isPermitted(Permission.ofRepositoryAdmin(repository))) {
			return true;
		} else {
			return review.getReviewer().equals(userManager.getCurrent());
		}
	}

	@Override
	public boolean canModifyComment(Comment comment) {
		User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
		if (currentUser == null) {
			return false;
		} else {
			if (currentUser.equals(comment.getUser())) {
				return true;
			} else {
				Permission adminPermission = Permission.ofRepositoryAdmin(comment.getRepository());
				return SecurityUtils.getSubject().isPermitted(adminPermission);
			}
		}
	}

	@Override
	public boolean canCreateBranch(Repository repository, String branchName) {
		User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
		return currentUser != null 
				&& currentUser.asSubject().isPermitted(Permission.ofRepositoryWrite(repository))	
				&& repository.getGateKeeper().checkRef(currentUser, repository, Git.REFS_HEADS + branchName).isPassed();
	}

	@Override
	public boolean canModifyBranch(Branch branch) {
		User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
		Repository repository = branch.getRepository();
		return currentUser != null 
				&& currentUser.asSubject().isPermitted(Permission.ofRepositoryWrite(repository))	
				&& repository.getGateKeeper().checkRef(currentUser, repository, Git.REFS_HEADS + branch.getName()).isPassed();
	}

}
