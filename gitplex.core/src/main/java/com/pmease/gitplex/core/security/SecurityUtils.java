package com.pmease.gitplex.core.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.lib.Constants;

import com.pmease.commons.git.GitUtils;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Comment;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.DepotAndBranch;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.model.Team;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.core.permission.operation.DepotOperation;

public class SecurityUtils extends org.apache.shiro.SecurityUtils {
	
	public static Collection<User> findUsersCan(Depot depot, DepotOperation operation) {
		Set<User> authorizedUsers = new HashSet<User>();
		for (User user: GitPlex.getInstance(Dao.class).query(EntityCriteria.of(User.class), 0, 0)) {
			if (user.asSubject().isPermitted(new ObjectPermission(depot, operation)))
				authorizedUsers.add(user);
		}
		return authorizedUsers;
	}

	public static boolean canModify(PullRequest request) {
		Depot depot = request.getTargetDepot();
		if (SecurityUtils.getSubject().isPermitted(ObjectPermission.ofDepotAdmin(depot))) {
			return true;
		} else {
			User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
			User submitter = request.getSubmitter();
			return currentUser != null && currentUser.equals(submitter);
		}
	}
	
	public static boolean canModify(Review review) {
		Depot depot = review.getUpdate().getRequest().getTargetDepot();
		if (SecurityUtils.getSubject().isPermitted(ObjectPermission.ofDepotAdmin(depot))) {
			return true;
		} else {
			return review.getReviewer().equals(GitPlex.getInstance(UserManager.class).getCurrent());
		}
	}
	
	public static boolean canModify(Comment comment) {
		User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
		if (currentUser == null) {
			return false;
		} else {
			if (currentUser.equals(comment.getUser())) {
				return true;
			} else {
				ObjectPermission adminPermission = ObjectPermission.ofDepotAdmin(comment.getDepot());
				return SecurityUtils.getSubject().isPermitted(adminPermission);
			}
		}
	}

	public static boolean canCreate(DepotAndBranch depotAndBranch) {
		return canCreate(depotAndBranch.getDepot(), depotAndBranch.getBranch());
	}
	
	public static boolean canCreate(Depot depot, String branch) {
		User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
		return currentUser != null 
				&& currentUser.asSubject().isPermitted(ObjectPermission.ofDepotPush(depot))	
				&& depot.getGateKeeper().checkRef(currentUser, depot, Constants.R_HEADS + branch).isPassed();
	}

	public static boolean canModify(DepotAndBranch depotAndBranch) {
		return canModify(depotAndBranch.getDepot(), GitUtils.branch2ref(depotAndBranch.getBranch()));
	}
	
	public static boolean canModify(Depot depot, String refName) {
		User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
		return currentUser != null 
				&& currentUser.asSubject().isPermitted(ObjectPermission.ofDepotPush(depot))	
				&& depot.getGateKeeper().checkRef(currentUser, depot, refName).isPassed();
	}
	
	public static boolean canManage(User account) {
		User currentUser = GitPlex.getInstance(UserManager.class).getCurrent();
		if (currentUser != null) {
			if (currentUser.isRoot())
				return true;
			
			for (Team team: currentUser.getTeams()) {
				if (team.isOwners() && team.getOwner().equals(account))
					return true;
			}
		}  
		return false;
	}
	
	public static boolean canPull(Depot depot) {
		return getSubject().isPermitted(ObjectPermission.ofDepotPull(depot));
	}
	
	public static boolean canPush(Depot depot) {
		return getSubject().isPermitted(ObjectPermission.ofDepotPush(depot));
	}
	
	public static boolean canManage(Depot depot) {
		return getSubject().isPermitted(ObjectPermission.ofDepotAdmin(depot));
	}

	public static boolean canManageSystem() {
		return getSubject().isPermitted(ObjectPermission.ofSystemAdmin());
	}
}
