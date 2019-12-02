package io.onedev.server.util;

import java.util.Collection;
import java.util.HashSet;

import org.apache.shiro.authz.Permission;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.Role;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.security.permission.AccessBuild;
import io.onedev.server.security.permission.AccessBuildLog;
import io.onedev.server.security.permission.AccessBuildReports;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.security.permission.CreateProjects;
import io.onedev.server.security.permission.EditIssueField;
import io.onedev.server.security.permission.JobPermission;
import io.onedev.server.security.permission.ManageBuilds;
import io.onedev.server.security.permission.ManageCodeComments;
import io.onedev.server.security.permission.ManageIssues;
import io.onedev.server.security.permission.ManageJob;
import io.onedev.server.security.permission.ManageProject;
import io.onedev.server.security.permission.ManagePullRequests;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.security.permission.RunJob;
import io.onedev.server.security.permission.ScheduleIssues;
import io.onedev.server.security.permission.SystemAdministration;
import io.onedev.server.security.permission.WriteCode;

public class SecurityUtils extends org.apache.shiro.SecurityUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
	
	public static Collection<User> getAuthorizedUsers(Project project, Permission permission) {
		Collection<User> authorizedUsers = new HashSet<>();
		authorizedUsers.add(OneDev.getInstance(UserManager.class).getRoot());
		if (project.getOwner() != null)
			authorizedUsers.add(project.getOwner());
		Group anonymousGroup = OneDev.getInstance(GroupManager.class).findAnonymous();
		for (GroupAuthorization authorization: project.getGroupAuthorizations()) {
			Group group = authorization.getGroup();
			if (group.isAdministrator() || authorization.getRole().implies(permission)) {
				if (group.equals(anonymousGroup))
					authorizedUsers.addAll(OneDev.getInstance(UserManager.class).query());
				else
					authorizedUsers.addAll(group.getMembers());
			}
		}

		for (UserAuthorization authorization: project.getUserAuthorizations()) {
			if (authorization.getRole().implies(permission))
				authorizedUsers.add(authorization.getUser());
		}
		
		return authorizedUsers;
	}
	
	public static User getUser() {
		return OneDev.getInstance(UserManager.class).getCurrent();
	}
	
	public static boolean canDeleteBranch(Project project, String branchName) {
		if (canWriteCode(project)) {
			BranchProtection protection = project.getBranchProtection(branchName, getUser());
			return protection == null || !protection.isNoDeletion();
		} else {
			return false;
		}
	}
	
	public static boolean canCreateTag(Project project, String tagName) {
		if (canWriteCode(project)) {
			TagProtection protection = project.getTagProtection(tagName, getUser());
			return protection == null || !protection.isNoCreation();
		} else {
			return false;
		}
	}
	
	public static boolean canCreateBranch(Project project, String branchName) {
		if (canWriteCode(project)) {
			BranchProtection protection = project.getBranchProtection(branchName, getUser());
			return protection == null || !protection.isNoCreation();
		} else {
			return false;
		}
	}
	
	public static boolean canModify(Project project, String branch, String file) {
		User user = getUser();
		return canWriteCode(project) 
				&& !project.isReviewRequiredForModification(user, branch, file)
				&& !project.isBuildRequiredForModification(user, branch, file); 
	}
	
	public static boolean canPush(Project project, String branch, ObjectId oldObjectId, ObjectId newObjectId) {
		User user = getUser();
		return canWriteCode(project) 
				&& !project.isReviewRequiredForPush(user, branch, oldObjectId, newObjectId, null)
				&& !project.isBuildRequiredForPush(user, branch, oldObjectId, newObjectId, null); 
	}
	
	public static boolean canEditIssueField(Project project, String fieldName) {
		return getSubject().isPermitted(new ProjectPermission(project, new EditIssueField(fieldName)));
	}
	
	public static boolean canScheduleIssues(Project project) {
		return getSubject().isPermitted(new ProjectPermission(project, new ScheduleIssues()));
	}
	
	public static boolean isAuthorizedWithRole(Project project, String roleName) {
		Role role = OneDev.getInstance(RoleManager.class).find(roleName);
		if (role != null) {
			User user = getUser();
			if (user != null) {
				for (UserAuthorization authorization: user.getProjectAuthorizations()) {
					if (project.equals(authorization.getProject()) && authorization.getRole().equals(role))
						return true;
				}
				for (Group group: user.getGroups()) {
					for (GroupAuthorization authorization: group.getProjectAuthorizations()) {
						if (project.equals(authorization.getProject()) && authorization.getRole().equals(role))
							return true;
					}
				}
			} 
			Group group = OneDev.getInstance(GroupManager.class).findAnonymous();
			if (group != null) {
				for (GroupAuthorization authorization: group.getProjectAuthorizations()) {
					if (project.equals(authorization.getProject()) && authorization.getRole().equals(role))
						return true;
				}
			}
			return false;
		} else {
			logger.error("Undefined role: " + roleName);
			return false;
		}
	}
	
	public static boolean canCreateProjects() {
		return getSubject().isPermitted(new CreateProjects());
	}
	
	public static boolean canAccess(Project project) {
		return getSubject().isPermitted(new ProjectPermission(project, new AccessProject()));
	}
	
	public static boolean canReadCode(Project project) {
		return getSubject().isPermitted(new ProjectPermission(project, new ReadCode()));
	}
	
	public static boolean canWriteCode(Project project) {
		return getSubject().isPermitted(new ProjectPermission(project, new WriteCode()));
	}
	
	public static boolean canManage(Project project) {
		return getSubject().isPermitted(new ProjectPermission(project, new ManageProject()));
	}
	
	public static boolean canManageIssues(Project project) {
		return getSubject().isPermitted(new ProjectPermission(project, new ManageIssues()));
	}
	
	public static boolean canManageBuilds(Project project) {
		return getSubject().isPermitted(new ProjectPermission(project, new ManageBuilds()));
	}
	
	public static boolean canManagePullRequests(Project project) {
		return getSubject().isPermitted(new ProjectPermission(project, new ManagePullRequests()));
	}
	
	public static boolean canManageCodeComments(Project project) {
		return getSubject().isPermitted(new ProjectPermission(project, new ManageCodeComments()));
	}
	
	public static boolean canManage(Build build) {
		return getSubject().isPermitted(new ProjectPermission(build.getProject(), 
				new JobPermission(build.getJobName(), new ManageJob())));
	}
	
	public static boolean canAccessLog(Build build) {
		return getSubject().isPermitted(new ProjectPermission(build.getProject(), 
				new JobPermission(build.getJobName(), new AccessBuildLog())));
	}
	
	public static boolean canAccess(Build build) {
		return getSubject().isPermitted(new ProjectPermission(build.getProject(), 
				new JobPermission(build.getJobName(), new AccessBuild())));
	}
	
	public static boolean canAccessReport(Build build, String reportName) {
		return getSubject().isPermitted(new ProjectPermission(build.getProject(), 
				new JobPermission(build.getJobName(), new AccessBuildReports(reportName))));
	}
	
	public static boolean canRunJob(Project project, String jobName) {
		return getSubject().isPermitted(new ProjectPermission(project, 
				new JobPermission(jobName, new RunJob())));
	}
	
	public static boolean isAdministrator() {
		return getSubject().isPermitted(new SystemAdministration());
	}
	
	public static boolean canModifyOrDelete(CodeComment comment) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(comment.getUser()) 
				|| canManageCodeComments(comment.getProject());
	}
	
	public static boolean canModifyOrDelete(CodeCommentReply reply) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(reply.getUser()) 
				|| canManageCodeComments(reply.getComment().getProject());
	}
	
	public static boolean canModifyOrDelete(PullRequestComment comment) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(comment.getUser()) 
				|| canManagePullRequests(comment.getRequest().getTargetProject());
	}
	
	public static boolean canModifyOrDelete(IssueComment comment) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(comment.getUser()) 
				|| canManageIssues(comment.getIssue().getProject());
	}
	
	public static boolean canModifyOrDelete(PullRequestChange change) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(change.getUser()) 
				|| canManagePullRequests(change.getRequest().getTargetProject());
	}
	
	public static boolean canModifyOrDelete(IssueChange change) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(change.getUser()) 
				|| canManageIssues(change.getIssue().getProject());
	}

	public static boolean canModify(PullRequest request) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(request.getSubmitter()) 
				|| canManagePullRequests(request.getTargetProject());
	}
	
	public static boolean canModify(Issue issue) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(issue.getSubmitter()) 
				|| canManageIssues(issue.getProject());
	}
	
}
