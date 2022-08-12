package io.onedev.server.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.onedev.commons.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.Role;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.security.permission.AccessBuild;
import io.onedev.server.security.permission.AccessBuildLog;
import io.onedev.server.security.permission.AccessBuildReports;
import io.onedev.server.security.permission.AccessConfidentialIssues;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.security.permission.ConfidentialIssuePermission;
import io.onedev.server.security.permission.CreateChildren;
import io.onedev.server.security.permission.CreateRootProjects;
import io.onedev.server.security.permission.EditIssueField;
import io.onedev.server.security.permission.EditIssueLink;
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
import io.onedev.server.util.concurrent.PrioritizedCallable;
import io.onedev.server.util.concurrent.PrioritizedRunnable;

public class SecurityUtils extends org.apache.shiro.SecurityUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
	
	@Nullable
	public static User getUser() {
		Long userId = getUserId();
		if (userId != 0L) 
			return OneDev.getInstance(UserManager.class).get(userId);
		else
			return null;
	}
	
	@Nullable
	public static User toUser(PrincipalCollection principals) {
		Long userId = (Long) principals.getPrimaryPrincipal();
		if (!userId.equals(0L))
			return OneDev.getInstance(UserManager.class).load(userId);
		else
			return null;
	}
	
	public static Long getUserId() {
        Object principal = SecurityUtils.getSubject().getPrincipal();
        Preconditions.checkNotNull(principal);
        return (Long) principal;
	}
	
	public static boolean canCreateRootProjects() {
		return SecurityUtils.getSubject().isPermitted(new CreateRootProjects());
	}
	
	public static boolean canCreateProjects() {
		if (canCreateRootProjects()) {
			return true;
		} else {
			ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
			return !projectManager.getPermittedProjects(new CreateChildren()).isEmpty();
		}
	}
	
	public static boolean canDeleteBranch(Project project, String branchName) {
		if (canWriteCode(project)) 
			return !project.getHierarchyBranchProtection(branchName, getUser()).isPreventDeletion();
		else 
			return false;
	}
	
	public static boolean canDeleteTag(Project project, String tagName) {
		if (canWriteCode(project)) 
			return !project.getHierarchyTagProtection(tagName, getUser()).isPreventDeletion();
		else 
			return false;
	}
	
	public static boolean canCreateTag(Project project, String tagName) {
		if (canWriteCode(project)) 
			return !project.getHierarchyTagProtection(tagName, getUser()).isPreventCreation();
		else 
			return false;
	}
	
	public static boolean canCreateBranch(Project project, String branchName) {
		if (canWriteCode(project)) 
			return !project.getHierarchyBranchProtection(branchName, getUser()).isPreventCreation();
		else 
			return false;
	}
	
	public static boolean canModify(Project project, String branch, String file) {
		User user = getUser();
		return canWriteCode(project) 
				&& !project.isCommitSignatureRequiredButNoSigningKey(user, branch) 
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
		return getSubject().isPermitted(new ProjectPermission(project, new EditIssueField(Sets.newHashSet(fieldName))));
	}
	
	public static boolean canEditIssueLink(Project project, LinkSpec link) {
		return getSubject().isPermitted(new ProjectPermission(project, new EditIssueLink(link)));
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
					Project authorizedProject = authorization.getProject();
					if (authorization.getRole().equals(role) && authorizedProject.isSelfOrAncestorOf(project)) 
						return true;
				}
				
				Set<Group> groups = new HashSet<>(user.getGroups());
				Group defaultLoginGroup = OneDev.getInstance(SettingManager.class)
						.getSecuritySetting().getDefaultLoginGroup();
				if (defaultLoginGroup != null)
					groups.add(defaultLoginGroup);
				
				for (Group group: groups) {
					for (GroupAuthorization authorization: group.getAuthorizations()) {
						Project authorizedProject = authorization.getProject();
						if (authorization.getRole().equals(role) && authorizedProject.isSelfOrAncestorOf(project)) 
							return true;
					}
				}
			} 
			return role.getDefaultProjects().stream().anyMatch(it->it.isSelfOrAncestorOf(project));
		} else {
			logger.error("Undefined role: " + roleName);
			return false;
		}
	}
	
	public static boolean canAccess(Project project) {
		return canAccess(getSubject(), project);
	}
	
	public static boolean canAccessConfidentialIssues(Project project) {
		return getSubject().isPermitted(new ProjectPermission(project, new AccessConfidentialIssues()));
	}
	
	public static boolean canAccess(Issue issue) {
		return canAccess(SecurityUtils.getSubject(), issue);
	}
	
	public static boolean canAccess(Subject subject, Project project) {
		return subject.isPermitted(new ProjectPermission(project, new AccessProject()));
	}
	
	public static boolean canAccess(Subject subject, Issue issue) {
		Permission permission = new ProjectPermission(issue.getProject(), new ConfidentialIssuePermission(issue));
		return issue.isConfidential() && subject.isPermitted(permission)
				|| !issue.isConfidential() && canAccess(subject, issue.getProject());
	}
	
	public static boolean canCreateChildren(Project project) {
		return getSubject().isPermitted(new ProjectPermission(project, new CreateChildren()));
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
	
	public static boolean canAccess(Project project, String jobName) {
		return getSubject().isPermitted(new ProjectPermission(project, 
				new JobPermission(jobName, new AccessBuild())));
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
	
    public static PrincipalCollection asPrincipal(Long userId) {
        return new SimplePrincipalCollection(userId, "");
    }
    
    public static Subject asSubject(Long userId) {
    	return asSubject(asPrincipal(userId));
    }
    
    public static Subject asSubject(PrincipalCollection principals) {
    	WebSecurityManager securityManager = AppLoader.getInstance(WebSecurityManager.class);
        return new Subject.Builder(securityManager).principals(principals).buildSubject();
    }
    
    public static Subject asAnonymous() {
    	return asSubject(0L);
    }
    
	public static void bindAsSystem() {
		ThreadContext.bind(asSubject(User.ONEDEV_ID));
	}
	
	public static Runnable inheritSubject(Runnable task) {
		Subject subject = SecurityUtils.getSubject();
		return new Runnable() {

			@Override
			public void run() {
				ThreadContext.bind(subject);
				task.run();
			}
			
		};
	}
	
	public static PrioritizedRunnable inheritSubject(PrioritizedRunnable task) {
		Subject subject = SecurityUtils.getSubject();
		return new PrioritizedRunnable(task.getPriority()) {

			@Override
			public void run() {
				ThreadContext.bind(subject);
				task.run();
			}
			
		};
	}
	
	public static <T> PrioritizedCallable<T> inheritSubject(PrioritizedCallable<T> task) {
		Subject subject = SecurityUtils.getSubject();
		return new PrioritizedCallable<T>(task.getPriority()) {

			@Override
			public T call() throws Exception {
				ThreadContext.bind(subject);
				return task.call();
			}
			
		};
	}
	
	public static <T> Callable<T> inheritSubject(Callable<T> callable) {
		Subject subject = SecurityUtils.getSubject();
		return new Callable<T>() {

			@Override
			public T call() throws Exception {
				ThreadContext.bind(subject);
				return callable.call();
			}

		};
	}
	
	public static <T> Collection<Callable<T>> inheritSubject(Collection<? extends Callable<T>> callables) {
		Subject subject = SecurityUtils.getSubject();
		Collection<Callable<T>> wrappedTasks = new ArrayList<>();
		for (Callable<T> task: callables) {
			wrappedTasks.add(new Callable<T>() {

				@Override
				public T call() throws Exception {
					ThreadContext.bind(subject);
					return task.call();
				}
				
			});
		}
		return wrappedTasks;
	}
	
}