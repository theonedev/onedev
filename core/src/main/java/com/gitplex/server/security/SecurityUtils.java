package com.gitplex.server.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.CacheManager;
import com.gitplex.server.manager.ReviewManager;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestComment;
import com.gitplex.server.model.PullRequestStatusChange;
import com.gitplex.server.model.Review;
import com.gitplex.server.model.User;
import com.gitplex.server.model.support.BranchProtection;
import com.gitplex.server.model.support.CodeCommentActivity;
import com.gitplex.server.model.support.TagProtection;
import com.gitplex.server.security.permission.CreateProjects;
import com.gitplex.server.security.permission.ProjectPermission;
import com.gitplex.server.security.permission.PublicPermission;
import com.gitplex.server.security.permission.SystemAdministration;
import com.gitplex.server.security.permission.UserAdministration;
import com.gitplex.server.util.facade.GroupAuthorizationFacade;
import com.gitplex.server.util.facade.GroupFacade;
import com.gitplex.server.util.facade.MembershipFacade;
import com.gitplex.server.util.facade.ProjectFacade;
import com.gitplex.server.util.facade.UserAuthorizationFacade;
import com.gitplex.server.util.facade.UserFacade;

public class SecurityUtils extends org.apache.shiro.SecurityUtils {
	
	public static Collection<UserFacade> getAuthorizedUsers(ProjectFacade project, ProjectPrivilege privilege) {
		CacheManager cacheManager = GitPlex.getInstance(CacheManager.class);
		Collection<UserFacade> authorizedUsers = new HashSet<>();
		if (project.isPublicRead() && privilege == ProjectPrivilege.READ) {
			for (UserFacade user: cacheManager.getUsers().values())
				authorizedUsers.add(user);
		} else {
			authorizedUsers.add(GitPlex.getInstance(UserManager.class).getRoot().getFacade());

			Set<Long> authorizedGroupIds = new HashSet<>();
			for (GroupFacade group: cacheManager.getGroups().values()) {
				if (group.isAdministrator()) 
					authorizedGroupIds.add(group.getId());
			}
			for (GroupAuthorizationFacade authorization: cacheManager.getGroupAuthorizations().values()) {
				if (authorization.getProjectId().equals(project.getId()) && authorization.getPrivilege().implies(privilege))
					authorizedGroupIds.add(authorization.getGroupId());
			}
			for (MembershipFacade membership: cacheManager.getMemberships().values()) {
				if (authorizedGroupIds.contains(membership.getGroupId()))
					authorizedUsers.add(cacheManager.getUser(membership.getUserId()));
			}

			for (UserAuthorizationFacade authorization: cacheManager.getUserAuthorizations().values()) {
				if (authorization.getProjectId().equals(project.getId()) 
						&& authorization.getPrivilege().implies(privilege)) {
					authorizedUsers.add(cacheManager.getUser(authorization.getUserId()));
				}
			}
		}
		
		return authorizedUsers;
	}

	public static boolean canModify(PullRequest request) {
		Project project = request.getTargetProject();
		if (canManage(project)) {
			return true;
		} else {
			User currentUser = getUser();
			return currentUser != null && currentUser.equals(request.getSubmitter());
		}
	}

	public static User getUser() {
		return GitPlex.getInstance(UserManager.class).getCurrent();
	}
	
	public static boolean canModify(Review review) {
		Project project = review.getRequest().getTargetProject();
		if (canManage(project)) {
			return true;
		} else {
			return review.getUser().equals(getUser());
		}
	}
	
	public static boolean canModify(CodeComment comment) {
		User currentUser = getUser();
		if (currentUser == null) {
			return false;
		} else {
			return currentUser.equals(comment.getUser()) || canManage(comment.getRequest().getTargetProject());
		}
	}

	public static boolean canModify(CodeCommentActivity activity) {
		User currentUser = getUser();
		if (currentUser == null) {
			return false;
		} else {
			return currentUser.equals(activity.getUser()) || canManage(activity.getComment().getRequest().getTargetProject());
		}
	}
	
	public static boolean canModify(PullRequestComment comment) {
		User currentUser = getUser();
		if (currentUser == null) {
			return false;
		} else {
			return currentUser.equals(comment.getUser()) || canModify(comment.getRequest());
		}
	}
	
	public static boolean canModify(PullRequestStatusChange statusChange) {
		User currentUser = getUser();
		if (currentUser == null) {
			return false;
		} else {
			return currentUser.equals(statusChange.getUser()) || canModify(statusChange.getRequest());
		}
	}
	
	public static boolean canDeleteBranch(Project project, String branchName) {
		if (canWrite(project)) {
			BranchProtection protection = project.getBranchProtection(branchName);
			return protection == null || !protection.isNoDeletion();
		} else {
			return false;
		}
	}
	
	public static boolean canUpdateTag(Project project, String tagName) {
		if (canWrite(project)) {
			TagProtection protection = project.getTagProtection(tagName);
			return protection == null || !protection.isNoUpdate();
		} else {
			return false;
		}
	}
	
	public static boolean canDeleteTag(Project project, String tagName) {
		if (canWrite(project)) {
			TagProtection protection = project.getTagProtection(tagName);
			return protection == null || !protection.isNoDeletion();
		} else {
			return false;
		}
	}
	
	@Nullable
	public static boolean canCreateTag(Project project, String tagName) {
		if (canWrite(project)) {
			TagProtection protection = project.getTagProtection(tagName);
			return protection == null || 
					protection.getTagCreator().getNotMatchMessage(project, SecurityUtils.getUser()) == null;
		} else {
			return false;
		}
	}
	
	public static boolean canModify(Project project, String branch, String file) {
		return canWrite(project) && GitPlex.getInstance(ReviewManager.class).canModify(getUser(), project, branch, file); 
	}
	
	public static boolean canPush(Project project, String branchName, ObjectId oldObjectId, ObjectId newObjectId) {
		return canWrite(project) && GitPlex.getInstance(ReviewManager.class).canPush(getUser(), project, branchName, 
				oldObjectId, newObjectId); 
	}
	
	public static boolean canManage(User user) {
		return canManage(user.getFacade());
	}
	
	public static boolean canManage(UserFacade user) {
		return getSubject().isPermitted(new UserAdministration(user));
	}
	
	public static boolean canCreateProjects() {
		return getSubject().isPermitted(new CreateProjects());
	}
	
	public static boolean canAccessPublic() {
		return getSubject().isPermitted(new PublicPermission());
	}
	
	public static boolean canRead(Project project) {
		return canRead(project.getFacade());
	}
	
	public static boolean canRead(ProjectFacade project) {
		return getSubject().isPermitted(new ProjectPermission(project, ProjectPrivilege.READ));
	}
	
	public static boolean canWrite(Project project) {
		return canWrite(project.getFacade());
	}

	public static boolean canWrite(ProjectFacade project) {
		return getSubject().isPermitted(new ProjectPermission(project, ProjectPrivilege.WRITE));
	}
	
	public static boolean canManage(Project project) {
		return canManage(project.getFacade());
	}
	
	public static boolean canManage(ProjectFacade project) {
		return getSubject().isPermitted(new ProjectPermission(project, ProjectPrivilege.ADMIN));
	}
	
	public static boolean isAdministrator() {
		return getSubject().isPermitted(new SystemAdministration());
	}
	
}
