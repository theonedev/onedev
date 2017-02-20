package com.gitplex.server.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestComment;
import com.gitplex.server.model.PullRequestReview;
import com.gitplex.server.model.PullRequestStatusChange;
import com.gitplex.server.model.support.CodeCommentActivity;
import com.gitplex.server.security.privilege.DepotPrivilege;
import com.gitplex.server.security.privilege.Privilege;

public class SecurityUtils extends org.apache.shiro.SecurityUtils {
	
	public static Collection<Account> findUsersCan(Depot depot, DepotPrivilege operation) {
		Set<Account> authorizedUsers = new HashSet<Account>();
		for (Account account: GitPlex.getInstance(AccountManager.class).findAll()) {
			if (!account.isOrganization() && account.asSubject().isPermitted(new ObjectPermission(depot, operation)))
				authorizedUsers.add(account);
		}
		return authorizedUsers;
	}

	public static boolean canModify(PullRequest request) {
		Depot depot = request.getTargetDepot();
		if (canManage(depot)) {
			return true;
		} else {
			Account currentUser = getAccount();
			return currentUser != null && 
					(currentUser.equals(request.getSubmitter()) || currentUser.equals(request.getAssignee()));
		}
	}

	public static Account getAccount() {
		return GitPlex.getInstance(AccountManager.class).getCurrent();
	}
	
	public static boolean canModify(PullRequestReview review) {
		Depot depot = review.getUpdate().getRequest().getTargetDepot();
		if (canManage(depot)) {
			return true;
		} else {
			return review.getUser().equals(getAccount());
		}
	}
	
	public static boolean canModify(CodeComment comment) {
		Account currentUser = getAccount();
		if (currentUser == null) {
			return false;
		} else {
			return currentUser.equals(comment.getUser()) || canManage(comment.getDepot());
		}
	}

	public static boolean canModify(CodeCommentActivity activity) {
		Account currentUser = getAccount();
		if (currentUser == null) {
			return false;
		} else {
			return currentUser.equals(activity.getUser()) || canManage(activity.getComment().getDepot());
		}
	}
	
	public static boolean canModify(PullRequestComment comment) {
		Account currentUser = getAccount();
		if (currentUser == null) {
			return false;
		} else {
			return currentUser.equals(comment.getUser()) || canModify(comment.getRequest());
		}
	}
	
	public static boolean canModify(PullRequestStatusChange statusChange) {
		Account currentUser = getAccount();
		if (currentUser == null) {
			return false;
		} else {
			return currentUser.equals(statusChange.getUser()) || canModify(statusChange.getRequest());
		}
	}
	
	public static boolean canPushRef(Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		Account currentUser = getAccount();
		return currentUser != null 
				&& currentUser.asSubject().isPermitted(ObjectPermission.ofDepotWrite(depot))	
				&& depot.getGateKeeper().checkPush(currentUser, depot, refName, oldCommit, newCommit).isPassedOrIgnored();
	}

	public static boolean canModify(Depot depot, String branch, @Nullable String file) {
		return canWrite(depot) && depot.getGateKeeper().checkFile(getAccount(), depot, branch, file).isPassedOrIgnored();
	}
	
	public static boolean canManage(Account account) {
		return getSubject().isPermitted(ObjectPermission.ofAccountAdmin(account));
	}
	
	public static boolean canAccess(Account organization) {
		return getSubject().isPermitted(ObjectPermission.ofAccountAccess(organization));
	}
	
	public static boolean canRead(Depot depot) {
		return getSubject().isPermitted(ObjectPermission.ofDepotRead(depot));
	}
	
	public static boolean canWrite(Depot depot) {
		return getSubject().isPermitted(ObjectPermission.ofDepotWrite(depot));
	}
	
	public static boolean canManage(Depot depot) {
		return getSubject().isPermitted(ObjectPermission.ofDepotAdmin(depot));
	}

	public static boolean canManageSystem() {
		Account currentUser = getAccount();
		return currentUser != null && currentUser.isAdministrator();
	}

	public static boolean isGreater(Privilege greater, Privilege lesser) {
		return greater.can(lesser) && !lesser.can(greater);
	}
	
}
