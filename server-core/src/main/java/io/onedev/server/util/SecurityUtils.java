package io.onedev.server.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.cache.CacheManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.User;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.security.permission.CreateProjects;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ProjectPrivilege;
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
		Collection<UserFacade> authorizedUsers = new HashSet<>();
		CacheManager cacheManager = OneDev.getInstance(CacheManager.class);
		if (project.getDefaultPrivilege() != null && project.getDefaultPrivilege().getProjectPrivilege().implies(privilege)) {
			authorizedUsers.addAll(cacheManager.getUsers().values());
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
	
	public static User getUser() {
		return OneDev.getInstance(UserManager.class).getCurrent();
	}
	
	public static boolean canDeleteBranch(Project project, String branchName) {
		if (canWriteCode(project.getFacade())) {
			BranchProtection protection = project.getBranchProtection(branchName, getUser());
			return protection == null || !protection.isNoDeletion();
		} else {
			return false;
		}
	}
	
	@Nullable
	public static boolean canCreateTag(Project project, String tagName) {
		if (canWriteCode(project.getFacade())) {
			TagProtection protection = project.getTagProtection(tagName, getUser());
			return protection == null || !protection.isNoCreation();
		} else {
			return false;
		}
	}
	
	@Nullable
	public static boolean canCreateBranch(Project project, String branchName) {
		if (canWriteCode(project.getFacade())) {
			BranchProtection protection = project.getBranchProtection(branchName, getUser());
			return protection == null || !protection.isNoCreation();
		} else {
			return false;
		}
	}
	
	public static boolean canModify(Project project, String branch, String file) {
		return canWriteCode(project.getFacade()) 
				&& !project.isReviewRequiredForModification(getUser(), branch, file)
				&& !project.isBuildRequiredForModification(branch, file); 
	}
	
	public static boolean canPush(Project project, String branch, ObjectId oldObjectId, ObjectId newObjectId) {
		return canWriteCode(project.getFacade()) 
				&& !project.isReviewRequiredForPush(getUser(), branch, oldObjectId, newObjectId, null)
				&& !project.isBuildRequiredForPush(branch, oldObjectId, newObjectId, null); 
	}
	
	public static boolean canAdministrate(UserFacade user) {
		return getSubject().isPermitted(new UserAdministration(user));
	}
	
	public static boolean canCreateProjects() {
		return getSubject().isPermitted(new CreateProjects());
	}
	
	public static boolean canReadIssues(ProjectFacade project) {
		return getSubject().isPermitted(new ProjectPermission(project, ProjectPrivilege.ISSUE_READ));
	}
	
	public static boolean canReadCode(ProjectFacade project) {
		return getSubject().isPermitted(new ProjectPermission(project, ProjectPrivilege.CODE_READ));
	}
	
	public static boolean canWriteCode(ProjectFacade project) {
		return getSubject().isPermitted(new ProjectPermission(project, ProjectPrivilege.CODE_WRITE));
	}
	
	public static boolean canAdministrate(ProjectFacade project) {
		return getSubject().isPermitted(new ProjectPermission(project, ProjectPrivilege.ADMINISTRATION));
	}
	
	public static boolean isAdministrator() {
		return getSubject().isPermitted(new SystemAdministration());
	}
	
	public static boolean canModifyOrDelete(CodeComment comment) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(comment.getUser()) 
				|| canWriteCode(comment.getProject().getFacade());
	}
	
	public static boolean canModifyOrDelete(CodeCommentReply reply) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(reply.getUser()) 
				|| canWriteCode(reply.getComment().getProject().getFacade());
	}
	
	public static boolean canModifyOrDelete(PullRequestComment comment) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(comment.getUser()) 
				|| canWriteCode(comment.getRequest().getTargetProject().getFacade());
	}
	
	public static boolean canModifyOrDelete(IssueComment comment) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(comment.getUser()) 
				|| canWriteCode(comment.getIssue().getProject().getFacade());
	}
	
	public static boolean canModifyOrDelete(PullRequestChange change) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(change.getUser()) 
				|| canWriteCode(change.getRequest().getTargetProject().getFacade());
	}
	
	public static boolean canModifyOrDelete(IssueChange change) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(change.getUser()) 
				|| canWriteCode(change.getIssue().getProject().getFacade());
	}

	public static boolean canModify(PullRequest request) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(request.getSubmitter()) 
				|| canWriteCode(request.getTargetProject().getFacade());
	}
	
	public static boolean canModify(Issue issue) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(issue.getSubmitter()) 
				|| canWriteCode(issue.getProject().getFacade());
	}
	
}
