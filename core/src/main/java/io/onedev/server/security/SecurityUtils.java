package io.onedev.server.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestStatusChange;
import io.onedev.server.model.Review;
import io.onedev.server.model.User;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.security.permission.CreateProjects;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.PublicPermission;
import io.onedev.server.security.permission.SystemAdministration;
import io.onedev.server.security.permission.UserAdministration;
import io.onedev.server.util.facade.GroupAuthorizationFacade;
import io.onedev.server.util.facade.GroupFacade;
import io.onedev.server.util.facade.MembershipFacade;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.UserAuthorizationFacade;
import io.onedev.server.util.facade.UserFacade;

public class SecurityUtils extends org.apache.shiro.SecurityUtils {
	
	public static Collection<UserFacade> getAuthorizedUsers(ProjectFacade project, ProjectPrivilege privilege) {
		CacheManager cacheManager = OneDev.getInstance(CacheManager.class);
		Collection<UserFacade> authorizedUsers = new HashSet<>();
		if (project.isPublicRead() && privilege == ProjectPrivilege.READ) {
			for (UserFacade user: cacheManager.getUsers().values())
				authorizedUsers.add(user);
		} else {
			authorizedUsers.add(OneDev.getInstance(UserManager.class).getRoot().getFacade());

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

	public static boolean canModify(Issue issue) {
		Project project = issue.getProject();
		if (canManage(project)) {
			return true;
		} else {
			User currentUser = getUser();
			return currentUser != null && currentUser.equals(issue.getReporter());
		}
	}
	
	public static User getUser() {
		return OneDev.getInstance(UserManager.class).getCurrent();
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
			return currentUser.equals(comment.getUser()) || canManage(comment.getProject());
		}
	}

	public static boolean canModify(CodeCommentReply reply) {
		User currentUser = getUser();
		if (currentUser == null) {
			return false;
		} else {
			return currentUser.equals(reply.getUser()) || canManage(reply.getComment().getProject());
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
			BranchProtection protection = project.getBranchProtection(branchName, getUser());
			return protection == null || !protection.isNoDeletion();
		} else {
			return false;
		}
	}
	
	@Nullable
	public static boolean canCreateTag(Project project, String tagName) {
		if (canWrite(project)) {
			TagProtection protection = project.getTagProtection(tagName, getUser());
			return protection == null || !protection.isNoCreation();
		} else {
			return false;
		}
	}
	
	@Nullable
	public static boolean canCreateBranch(Project project, String branchName) {
		if (canWrite(project)) {
			BranchProtection protection = project.getBranchProtection(branchName, getUser());
			return protection == null || !protection.isNoCreation();
		} else {
			return false;
		}
	}
	
	public static boolean canModify(Project project, String branch, String file) {
		return canWrite(project) && !OneDev.getInstance(ProjectManager.class)
				.isModificationNeedsQualityCheck(getUser(), project, branch, file); 
	}
	
	public static boolean canPush(Project project, String branchName, ObjectId oldObjectId, ObjectId newObjectId) {
		return canWrite(project) && !OneDev.getInstance(ProjectManager.class)
				.isPushNeedsQualityCheck(getUser(), project, branchName, oldObjectId, newObjectId, null); 
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
