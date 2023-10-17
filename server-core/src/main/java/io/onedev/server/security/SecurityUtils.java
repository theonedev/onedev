package io.onedev.server.security;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import io.onedev.commons.loader.AppLoader;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.*;
import io.onedev.server.security.permission.*;
import io.onedev.server.util.concurrent.PrioritizedCallable;
import io.onedev.server.util.concurrent.PrioritizedRunnable;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

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
			return !project.getBranchProtection(branchName, getUser()).isPreventDeletion();
		else 
			return false;
	}
	
	public static boolean canDeleteTag(Project project, String tagName) {
		if (canWriteCode(project)) 
			return !project.getTagProtection(tagName, getUser()).isPreventDeletion();
		else 
			return false;
	}
	
	public static boolean canCreateTag(Project project, String tagName) {
		return canCreateTag(getUser(), project, tagName);
	}

	public static boolean canCreateTag(@Nullable User user, Project project, String tagName) {
		if (canWriteCode(user, project))
			return !project.getTagProtection(tagName, user).isPreventCreation();
		else
			return false;
	}
	
	public static boolean canCreateBranch(@Nullable User user, Project project, String branchName) {
		if (canWriteCode(user, project)) 
			return !project.getBranchProtection(branchName, user).isPreventCreation();
		else 
			return false;
	}

	public static boolean canCreateBranch(Project project, String branchName) {
		return canCreateBranch(getUser(), project, branchName);
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

	public static boolean isAssignedRole(Project project, Role role) {
		return isAssignedRole(getUser(), project, role);
	}
	
	public static boolean isAssignedRole(@Nullable User user, Project project, Role role) {
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
		return canWriteCode(getUser(), project);
	}

	public static boolean canWriteCode(@Nullable User user, Project project) {
		return asSubject(user).isPermitted(new ProjectPermission(project, new WriteCode()));
	}
	
	public static boolean canManage(Project project) {
		return getSubject().isPermitted(new ProjectPermission(project, new ManageProject()));
	}
	
	public static boolean canManageIssues(Project project) {
		return canManageIssues(getUser(), project);
	}

	public static boolean canManageIssues(@Nullable User user, Project project) {
		return asSubject(user).isPermitted(new ProjectPermission(project, new ManageIssues()));
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

	public static boolean canModifyOrDelete(IssueWork work) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(work.getUser())
				|| canManageIssues(work.getIssue().getProject());
	}

	public static boolean canModifyOrDelete(IssueVote vote) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(vote.getUser())
				|| canManageIssues(vote.getIssue().getProject());
	}

	public static boolean canModifyOrDelete(IssueWatch watch) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(watch.getUser())
				|| canManageIssues(watch.getIssue().getProject());
	}
	
	public static boolean canModify(PullRequest request) {
		User user = SecurityUtils.getUser();
		return user != null && user.equals(request.getSubmitter()) 
				|| user != null && request.getAssignees().contains(user)
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
	
	public static Subject asSubject(@Nullable User user) {
		if (user != null)
			return asSubject(user.getId());
		else 
			return asAnonymous();
	}
    
	public static void bindAsSystem() {
		ThreadContext.bind(asSubject(User.SYSTEM_ID));
	}
	
	public static Runnable inheritSubject(Runnable task) {
		Subject subject = SecurityUtils.getSubject();
		return () -> {
			ThreadContext.bind(subject);
			task.run();
		};
	}
	
	public static Long getPrevUserId() {
		PrincipalCollection prevPrincipals = SecurityUtils.getSubject().getPreviousPrincipals();
		if (prevPrincipals != null) 
			return (Long) prevPrincipals.getPrimaryPrincipal();
		else 
			return 0L;
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
	
	@Nullable
	public static String getBearerToken(HttpServletRequest request) {
		String authHeader = request.getHeader(KubernetesHelper.AUTHORIZATION);
		if (authHeader == null)
			authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authHeader != null && authHeader.startsWith(KubernetesHelper.BEARER + " "))
			return authHeader.substring(KubernetesHelper.BEARER.length() + 1);
		else
			return null;
	}
	
}