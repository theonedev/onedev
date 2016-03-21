package com.pmease.gitplex.core.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.TeamAuthorization;
import com.pmease.gitplex.core.entity.Comment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.Team;
import com.pmease.gitplex.core.entity.TeamMembership;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.core.permission.privilege.DepotPrivilege;

public class SecurityUtils extends org.apache.shiro.SecurityUtils {
	
	public static Collection<Account> findUsersCan(Depot depot, DepotPrivilege operation) {
		Set<Account> authorizedUsers = new HashSet<Account>();
		for (Account user: GitPlex.getInstance(AccountManager.class).all()) {
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
			Account currentUser = getAccount();
			Account submitter = request.getSubmitter();
			return currentUser != null && currentUser.equals(submitter);
		}
	}

	public static Account getAccount() {
		return GitPlex.getInstance(AccountManager.class).getCurrent();
	}
	
	public static boolean canModify(Review review) {
		Depot depot = review.getUpdate().getRequest().getTargetDepot();
		if (SecurityUtils.getSubject().isPermitted(ObjectPermission.ofDepotAdmin(depot))) {
			return true;
		} else {
			return review.getReviewer().equals(getAccount());
		}
	}
	
	public static boolean canModify(Comment comment) {
		Account currentUser = getAccount();
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

	public static boolean canPushRef(Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		Account currentUser = getAccount();
		return currentUser != null 
				&& currentUser.asSubject().isPermitted(ObjectPermission.ofDepotWrite(depot))	
				&& depot.getGateKeeper().checkPush(currentUser, depot, refName, oldCommit, newCommit).isPassed();
	}
	
	public static boolean canManage(Account account) {
		return getSubject().isPermitted(ObjectPermission.ofAccountAdmin(account));
	}
	
	public static boolean isMemberOf(Account account) {
		return getSubject().isPermitted(ObjectPermission.ofAccountMember(account));
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

	private static Collection<Team> getAuthorizedTeams(Collection<TeamAuthorization> authorizations, 
			Depot depot, DepotPrivilege privilege) {
		Collection<Team> teams = new HashSet<>();
		for (TeamAuthorization authorization: authorizations) {
			if (authorization.getDepot().equals(depot) && authorization.getPrivilege() == privilege) {
				teams.add(authorization.getTeam());
			}
		}
		return teams;
	}
	
	private static Collection<Account> getTeamMembers(Collection<TeamMembership> memberships, Collection<Team> teams) {
		Collection<Account> members = new HashSet<>();
		for (TeamMembership membership: memberships) {
			if (teams.contains(membership.getTeam()))
				members.add(membership.getUser());
		}
		return members;
	}

	private static Collection<Account> getDepotAdministrators(TeamAuthorization authorization) {
		Account organization = authorization.getDepot().getAccount();
		Collection<Account> depotAdministrators = new HashSet<>();
		for (TeamMembership membership: authorization.getTeam().getMemberships()) {
			Account user = membership.getUser();
			if (user.isAdministrator() 
					|| organization.getOrganizationAdministrators().contains(user)
					|| organization.getDefaultPrivilege() == DepotPrivilege.ADMIN) {
				depotAdministrators.add(user);
			} else {
				Collection<Team> adminTeams = getAuthorizedTeams(
						organization.getAllTeamAuthorizationsInOrganization(), 
						authorization.getDepot(), 
						DepotPrivilege.ADMIN);
				Collection<Account> adminTeamMembers = getTeamMembers(
						organization.getAllTeamMembershipsInOrganiation(), 
						adminTeams);
				if (adminTeamMembers.contains(user))
					depotAdministrators.add(user);
			}
		}
		return depotAdministrators;
	}
	
	public static Map<Account, DepotPrivilege> getGreaterPrivileges(TeamAuthorization authorization) {
		Map<Account, DepotPrivilege> greaterPrivileges = new HashMap<>();
		Account organization = authorization.getDepot().getAccount();
		DepotPrivilege privilege = authorization.getPrivilege();
		if (privilege == DepotPrivilege.WRITE) {
			for (Account user: getDepotAdministrators(authorization)) {
				greaterPrivileges.put(user, DepotPrivilege.ADMIN);
			}
		} else if (privilege == DepotPrivilege.READ) {
			Collection<Account> depotWriters = new HashSet<>();
			for (TeamMembership membership: authorization.getTeam().getMemberships()) {
				Account user = membership.getUser();
				if (organization.getDefaultPrivilege() == DepotPrivilege.WRITE) {
					depotWriters.add(user);
				} else {
					Collection<Team> writeTeams = getAuthorizedTeams(organization.getAllTeamAuthorizationsInOrganization(), 
							authorization.getDepot(), DepotPrivilege.WRITE);
					Collection<Account> writeTeamMembers = getTeamMembers(organization.getAllTeamMembershipsInOrganiation(), 
							writeTeams);
					if (writeTeamMembers.contains(user))
						depotWriters.add(user);
				}
			}
			for (Account user: depotWriters) {
				greaterPrivileges.put(user, DepotPrivilege.WRITE);
			}
			for (Account user: getDepotAdministrators(authorization)) {
				greaterPrivileges.put(user, DepotPrivilege.ADMIN);
			}
		}
		return greaterPrivileges;
	}
	
}
