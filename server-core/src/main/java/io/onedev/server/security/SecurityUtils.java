package io.onedev.server.security;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.mgt.WebSecurityManager;

import com.google.common.collect.Sets;

import io.onedev.commons.loader.AppLoader;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.BaseAuthorizationService;
import io.onedev.server.service.GroupService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.model.AccessToken;
import io.onedev.server.model.Build;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueAuthorization;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueVote;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.IssueWork;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.Role;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.security.permission.AccessBuild;
import io.onedev.server.security.permission.AccessBuildLog;
import io.onedev.server.security.permission.AccessBuildPipeline;
import io.onedev.server.security.permission.AccessBuildReports;
import io.onedev.server.security.permission.AccessConfidentialIssues;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.security.permission.AccessTimeTracking;
import io.onedev.server.security.permission.BasePermission;
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
import io.onedev.server.security.permission.ReadPack;
import io.onedev.server.security.permission.RunJob;
import io.onedev.server.security.permission.ScheduleIssues;
import io.onedev.server.security.permission.SystemAdministration;
import io.onedev.server.security.permission.UploadCache;
import io.onedev.server.security.permission.WriteCode;
import io.onedev.server.security.permission.WritePack;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.util.facade.UserCache;
import io.onedev.server.util.facade.UserFacade;

public class SecurityUtils extends org.apache.shiro.SecurityUtils {

	public static final String PRINCIPAL_ANONYMOUS = "anonymous";
	
	public static final PrincipalCollection PRINCIPALS_ANONYMOUS = 
			new SimplePrincipalCollection(PRINCIPAL_ANONYMOUS, "");
	
	public static void checkPermission(Permission permission) {
		if (!getSubject().isPermitted(permission))
			throw new UnauthorizedException();
	}
	
	public static PrincipalCollection asPrincipals(String principal) {
		return new SimplePrincipalCollection(principal, "");
	}
	
	public static Subject asSubject(PrincipalCollection principals) {
		WebSecurityManager securityManager = AppLoader.getInstance(WebSecurityManager.class);
		return new Subject.Builder(securityManager).principals(principals).buildSubject();
	}

	public static Subject asSubject(@Nullable User user) {
		if (user == null)
			return asAnonymous();
		else if (user.isSystem())
			return asSystem();
		else
			return asSubject(asPrincipals(asUserPrincipal(user.getId())));
	}
	
	public static boolean isAnonymous(String principal) {
		return principal.equals(PRINCIPAL_ANONYMOUS);
	}
	
	public static boolean isAnonymous() {
		return isAnonymous((String) getSubject().getPrincipal());
	}

	public static boolean isSystem(String principal) {
		return User.SYSTEM_ID.equals(getAuthUserId(principal));
	}

	public static boolean isSystem() {
		return isSystem((String) getSubject().getPrincipal());
	}
	
	public static Subject asAnonymous() {
		return asSubject(PRINCIPALS_ANONYMOUS);
	}

	public static Subject asSystem() {
		return asSubject(asPrincipals(asUserPrincipal(User.SYSTEM_ID)));
	}
	
	public static String asUserPrincipal(Long userId) {
		return "u:" + userId;
	}
	
	@Nullable
	private static Long getAuthUserId(String principal) {
		if (principal.startsWith("u:"))
			return Long.valueOf(principal.substring(2));
		else
			return null;
	}
	
	@Nullable
	public static User getAuthUser(String principal) {
		var userId = getAuthUserId(principal);
		if (userId != null) {
			var user = OneDev.getInstance(UserService.class).get(userId);
			if (user != null && !user.isDisabled())
				return user;
		}
		return null;
	}

	@Nullable
	public static User getAuthUser() {
		return getAuthUser(getSubject());
	}
	
	@Nullable
	private static User getAuthUser(Subject subject) {
		return getAuthUser((String) subject.getPrincipal());
	}

	@Nullable
	public static User getUser(Subject subject) {
		String principal = (String) subject.getPrincipal();
		return getUser(getAuthUser(principal), getAccessToken(principal));
	}

	@Nullable
	public static User getUser(@Nullable User user, @Nullable AccessToken accessToken) {
		if (user != null)
			return user;
		else if (accessToken != null)
			return accessToken.getOwner();
		else
			return null;
	}
	
	@Nullable
	public static User getUser() {
		return getUser(getSubject());
	}
	
	public static String asAccessTokenPrincipal(Long accessTokenId) {
		return "a:" + accessTokenId;
	}
	
	@Nullable
	private static Long getAccessTokenId(String principal) {
		if (principal.startsWith("a:"))
			return Long.valueOf(principal.substring(2));
		else
			return null;
	}

	@Nullable
	public static AccessToken getAccessToken(String principal) {
		var accessTokenId = getAccessTokenId(principal);
		if (accessTokenId != null)
			return OneDev.getInstance(AccessTokenService.class).get(accessTokenId);
		else
			return null;
	}
	
	@Nullable
	public static String createTemporalAccessTokenIfUserPrincipal(long temporalAccessTokenExpireSeconds) {
		String principal = (String) getSubject().getPrincipal();
		var accessToken = getAccessToken(principal);
		if (accessToken != null) {
			return accessToken.getValue();
		} else {
			var userId = getAuthUserId(principal);			
			if (userId != null)
				return OneDev.getInstance(AccessTokenService.class).createTemporal(userId, temporalAccessTokenExpireSeconds);
			else
				return null;
		}
	}
	
	public static boolean canCreateRootProjects() {
		return canCreateRootProjects(SecurityUtils.getSubject());
	}

	public static boolean canCreateRootProjects(Subject subject) {
		return subject.isPermitted(new CreateRootProjects());
	}

	public static boolean canDeleteBranch(Subject subject, Project project, String branchName) {
		if (canWriteCode(subject, project))
			return !project.getBranchProtection(branchName, getUser(subject)).isPreventDeletion();
		else
			return false;
	}
	
	public static boolean canDeleteBranch(Project project, String branchName) {
		return canDeleteBranch(SecurityUtils.getSubject(), project, branchName);
	}

	public static boolean canDeleteTag(Subject subject, Project project, String tagName) {
		if (canWriteCode(subject, project))
			return !project.getTagProtection(tagName, getUser(subject)).isPreventDeletion();
		else
			return false;
	}
	
	public static boolean canDeleteTag(Project project, String tagName) {
		return canDeleteTag(SecurityUtils.getSubject(), project, tagName);
	}
	
	public static boolean canCreateTag(Project project, String tagName) {
		return canCreateTag(getSubject(), project, tagName);
	}

	public static boolean canCreateTag(Subject subject, Project project, String tagName) {
		if (canWriteCode(subject, project))
			return !project.getTagProtection(tagName, getUser(subject)).isPreventCreation();
		else
			return false;
	}
	
	public static boolean canCreateBranch(Subject subject, Project project, String branchName) {
		if (canWriteCode(subject, project)) 
			return !project.getBranchProtection(branchName, getUser(subject)).isPreventCreation();
		else 
			return false;
	}

	public static boolean canCreateBranch(Project project, String branchName) {
		return canCreateBranch(getSubject(), project, branchName);
	}
	
	public static boolean canModifyFile(Project project, String branch, String file) {
		var subject = getSubject();
		var user = getUser(subject);
		return canWriteCode(subject, project) 
				&& !project.isCommitSignatureRequiredButNoSigningKey(user, branch) 
				&& !project.isReviewRequiredForModification(user, branch, file)
				&& !project.isBuildRequiredForModification(user, branch, file); 
	}

	/**
	 * This method checks if current user is able to open terminal of specified build. 
	 * Terminal access means that the user can view secrets used in the build as well 
	 * as modifying build logic. So as long as the user can modify build spec of the 
	 * build, it should be allowed to open terminal
	 * 
	 * @param build
	 * @return
	 */
	public static boolean canOpenTerminal(Subject subject, Build build) {
		var user = getAuthUser(subject);
		if (user == null)
			return false;
		var project = build.getProject();
		if (SecurityUtils.canManageProject(subject, project) 
				|| build.getRequest() != null && build.getRequest().getSubmitter().equals(user)) {
			return true;
		}
		if (!canWriteCode(subject, project))
			return false;
		for (var branch: project.getReachableBranches(build.getCommitId())) {
			if (!project.canModifyBuildSpecRoughly(user, branch))
				return false;
		}
		return true;
	}
	
	public static boolean canOpenTerminal(Build build) {
		return canOpenTerminal(getSubject(), build);
	}
	
	public static boolean canEditIssueField(Project project, String fieldName) {
		return getSubject().isPermitted(new ProjectPermission(project, new EditIssueField(Sets.newHashSet(fieldName))));
	}
	
	public static boolean canEditIssueLink(Project project, LinkSpec link) {
		return getSubject().isPermitted(new ProjectPermission(project, new EditIssueLink(link)));
	}
	
	public static boolean canScheduleIssues(Project project) {
		return canScheduleIssues(getSubject(), project);
	}

	public static boolean canScheduleIssues(Subject subject, Project project) {
		return subject.isPermitted(new ProjectPermission(project, new ScheduleIssues()));
	}

	public static boolean isAssignedRole(Project project, Role role) {
		return isAssignedRole(getSubject(), project, role);
	}

	public static boolean isAssignedRole(Subject subject, Project project, Role role) {
		String principal = (String) subject.getPrincipal();
		var user = getAuthUser(principal);
		if (user != null) {
			for (UserAuthorization authorization: user.getProjectAuthorizations()) {
				Project authorizedProject = authorization.getProject();
				if (authorization.getRole().equals(role) && authorizedProject.isSelfOrAncestorOf(project))
					return true;
			}

			for (Group group: user.getGroups()) {
				for (GroupAuthorization authorization: group.getAuthorizations()) {
					Project authorizedProject = authorization.getProject();
					if (authorization.getRole().equals(role) && authorizedProject.isSelfOrAncestorOf(project))
						return true;
				}
			}

			return isAssignedDefaultRole(project, role);
		}
		
		var accessToken = getAccessToken(principal);
		if (accessToken != null) {
			for (var authorization: accessToken.getAuthorizations()) {
				Project authorizedProject = authorization.getProject();
				if (authorization.getRole().equals(role) && authorizedProject.isSelfOrAncestorOf(project))
					return true;
			}			
			return isAssignedDefaultRole(project, role);
		}
		return OneDev.getInstance(SettingService.class).getSecuritySetting().isEnableAnonymousAccess()
				&& isAssignedDefaultRole(project, role);
	}
	
	private static boolean isAssignedDefaultRole(Project project, Role role) {
		return role.getBaseAuthorizations().stream().anyMatch(it->it.getProject().isSelfOrAncestorOf(project));
	}
	
	public static boolean canAccessProject(Project project) {
		return canAccessProject(getSubject(), project);
	}
	
	public static boolean canAccessConfidentialIssues(Project project) {
		return canAccessConfidentialIssues(getSubject(), project);
	}

	public static boolean canAccessConfidentialIssues(Subject subject, Project project) {
		return subject.isPermitted(new ProjectPermission(project, new AccessConfidentialIssues()));
	}
	
	public static boolean canAccessTimeTracking(Project project) {
		return getSubject().isPermitted(new ProjectPermission(project, new AccessTimeTracking()));
	}
	
	public static boolean canAccessIssue(Issue issue) {
		return canAccessIssue(getSubject(), issue);
	}
	
	public static boolean canAccessProject(Subject subject, Project project) {
		return subject.isPermitted(new ProjectPermission(project, new AccessProject()));
	}
	
	public static boolean canAccessIssue(Subject subject, Issue issue) {
		Permission permission = new ProjectPermission(issue.getProject(), new ConfidentialIssuePermission(issue));
		return issue.isConfidential() && subject.isPermitted(permission)
				|| !issue.isConfidential() && canAccessProject(subject, issue.getProject());
	}
	
	public static boolean canCreateChildren(Project project) {
		return canCreateChildren(getSubject(), project);
	}

	public static boolean canCreateChildren(Subject subject, Project project) {
		return subject.isPermitted(new ProjectPermission(project, new CreateChildren()));
	}
	
	public static boolean canReadCode(Project project) {
		return canReadCode(getSubject(), project);
	}

	public static boolean canReadCode(Subject subject, Project project) {
		return subject.isPermitted(new ProjectPermission(project, new ReadCode()));
	}
	
	public static boolean canWriteCode(Project project) {
		return canWriteCode(getSubject(), project);
	}

	public static boolean canWriteCode(Subject subject, Project project) {
		return subject.isPermitted(new ProjectPermission(project, new WriteCode()));
	}
	
	public static boolean canManageProject(Project project) {
		return canManageProject(getSubject(), project);
	}

	public static boolean canManageProject(Subject subject, Project project) {
		return subject.isPermitted(new ProjectPermission(project, new ManageProject()));
	}
	
	public static boolean canManageIssues(Project project) {
		return canManageIssues(getSubject(), project);
	}

	public static boolean canManageIssues(Subject subject, Project project) {
		return subject.isPermitted(new ProjectPermission(project, new ManageIssues()));
	}
	
	public static boolean canManageBuilds(Project project) {
		return canManageBuilds(getSubject(), project);
	}

	public static boolean canManageBuilds(Subject subject, Project project) {
		return subject.isPermitted(new ProjectPermission(project, new ManageBuilds()));
	}

	public static boolean canManagePullRequests(Project project) {
		return canManagePullRequests(getSubject(), project);
	}
	
	public static boolean canManagePullRequests(Subject subject, Project project) {
		return subject.isPermitted(new ProjectPermission(project, new ManagePullRequests()));
	}
	
	public static boolean canManageCodeComments(Project project) {
		return canManageCodeComments(getSubject(), project);
	}

	public static boolean canManageCodeComments(Subject subject, Project project) {
		return subject.isPermitted(new ProjectPermission(project, new ManageCodeComments()));
	}
	
	public static boolean canManageBuild(Build build) {
		return getSubject().isPermitted(new ProjectPermission(build.getProject(), 
				new JobPermission(build.getJobName(), new ManageJob())));
	}

	public static boolean canUploadCache(Project project) {
		return canUploadCache(getSubject(), project);
	}

	public static boolean canUploadCache(Subject subject, Project project) {
		return subject.isPermitted(new ProjectPermission(project, new UploadCache()));
	}
	
	public static boolean canAccessLog(Build build) {
		return getSubject().isPermitted(new ProjectPermission(build.getProject(), 
				new JobPermission(build.getJobName(), new AccessBuildLog())));
	}

	public static boolean canAccessPipeline(Build build) {
		return getSubject().isPermitted(new ProjectPermission(build.getProject(),
				new JobPermission(build.getJobName(), new AccessBuildPipeline())));
	}
	
	public static boolean canAccessBuild(Build build) {
		return canAccessJob(build.getProject(), build.getJobName());
	}
	
	public static boolean canAccessJob(Project project, String jobName) {
		return getSubject().isPermitted(new ProjectPermission(project, 
				new JobPermission(jobName, new AccessBuild())));
	}

	public static boolean canReadPack(Project project) {
		return getSubject().isPermitted(new ProjectPermission(project, new ReadPack()));
	}
		
	public static boolean canAccessReport(Build build, String reportName) {
		return getSubject().isPermitted(new ProjectPermission(build.getProject(), 
				new JobPermission(build.getJobName(), new AccessBuildReports(reportName))));
	}
	
	public static boolean canRunJob(Project project, String jobName) {
		return canRunJob(getSubject(), project, jobName);
	}

	public static boolean canRunJob(Subject subject, Project project, String jobName) {
		return subject.isPermitted(new ProjectPermission(project, 
				new JobPermission(jobName, new RunJob())));
	}

	public static boolean canWritePack(Project project) {
		return getSubject().isPermitted(new ProjectPermission(project, new WritePack()));
	}
	
	public static boolean isAdministrator() {
		return isAdministrator(getSubject());
	}
	
	public static boolean isAdministrator(Subject subject) {
		return subject.isPermitted(new SystemAdministration());
	}
	
	public static boolean canModifyOrDelete(CodeComment comment) {
		return canModifyOrDelete(getSubject(), comment);
	}

	public static boolean canModifyOrDelete(Subject subject, CodeComment comment) {
		return canManageCodeComments(subject, comment.getProject()) 
				|| comment.getUser().equals(getAuthUser(subject));
	}
	
	public static boolean canModifyOrDelete(CodeCommentReply reply) {
		var subject = getSubject();
		return canManageCodeComments(subject, reply.getComment().getProject()) 
				|| reply.getUser().equals(getAuthUser(subject));
	}
	
	public static boolean canModifyOrDelete(PullRequestComment comment) {
		var subject = getSubject();
		return canManagePullRequests(subject, comment.getRequest().getTargetProject())
				|| comment.getUser().equals(getAuthUser(subject));
	}
	
	public static boolean canModifyOrDelete(IssueComment comment) {
		var subject = getSubject();
		return canManageIssues(subject, comment.getIssue().getProject())
				|| comment.getUser().equals(getAuthUser(subject));
	}

	public static boolean canModifyOrDelete(IssueWork work) {
		var subject = getSubject();
		return canManageIssues(subject, work.getIssue().getProject())
				|| work.getUser().equals(getAuthUser(subject));
	}

	public static boolean canModifyOrDelete(IssueVote vote) {
		var subject = getSubject();
		return canManageIssues(subject, vote.getIssue().getProject())
				|| vote.getUser().equals(getAuthUser(subject));
	}

	public static boolean canModifyOrDelete(IssueWatch watch) {
		var subject = getSubject();
		return canManageIssues(subject, watch.getIssue().getProject())
				|| watch.getUser().equals(getAuthUser(subject));
	}

	public static boolean canModifyOrDelete(PullRequestWatch watch) {
		var subject = getSubject();
		return canManagePullRequests(subject, watch.getRequest().getProject())
				|| watch.getUser().equals(getAuthUser(subject));
	}

	public static boolean canModifyOrDelete(PullRequestReview review) {
		var subject = getSubject();
		return canModifyPullRequest(review.getRequest()) || review.getUser().equals(getAuthUser(subject));
	}
	
	public static boolean canModifyPullRequest(PullRequest request) {
		return canModifyPullRequest(getSubject(), request);
	}

	public static boolean canModifyPullRequest(Subject subject, PullRequest request) {
		var user = getAuthUser(subject);
		return canManagePullRequests(subject, request.getTargetProject())
				|| user != null && (user.equals(request.getSubmitter()) || request.getAssignees().contains(user));
	}
	
	public static boolean canModifyIssue(Issue issue) {
		return canModifyIssue(getSubject(), issue);
	}

	public static boolean canModifyIssue(Subject subject, Issue issue) {
		return canManageIssues(subject, issue.getProject())
				|| issue.getSubmitter().equals(getAuthUser(subject));
	}
	
	public static void bindAsSystem() {
		ThreadContext.bind(asSystem());
	}
	
	public static Runnable inheritSubject(Runnable task) {
		Subject subject = SecurityUtils.getSubject();
		return () -> {
			ThreadContext.bind(subject);
			task.run();
		};
	}

	public static <T> Callable<T> inheritSubject(Callable<T> task) {
		Subject subject = SecurityUtils.getSubject();
		return new Callable<T>() {

			@Override
			public T call() throws Exception {
				ThreadContext.bind(subject);
				return task.call();
			}
			
		};
	}
	
	public static String getPrevPrincipal() {
		PrincipalCollection prevPrincipals = SecurityUtils.getSubject().getPreviousPrincipals();
		if (prevPrincipals != null) 
			return (String) prevPrincipals.getPrimaryPrincipal();
		else 
			return PRINCIPAL_ANONYMOUS;
	}
		
	@Nullable
	public static String getBearerToken(HttpServletRequest request) {
		String authHeader = request.getHeader(KubernetesHelper.AUTHORIZATION);
		if (authHeader == null)
			authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authHeader != null) {
			if (authHeader.startsWith(KubernetesHelper.BEARER + " "))
				return authHeader.substring(KubernetesHelper.BEARER.length() + 1);
			else if (authHeader.startsWith("token "))
				return authHeader.substring("token".length() + 1);
		} 
		return null;
	}

	private static void addIdsPermittedByDefaultRole(ProjectCache cache, Collection<Long> projectIds,
											  Permission permission) {
		var baseAuthorizationService = OneDev.getInstance(BaseAuthorizationService.class);
		for (var authorization: baseAuthorizationService.query()) {
			if (authorization.getRole().implies(permission))
				projectIds.addAll(cache.getSubtreeIds(authorization.getProject().getId()));
		}
	}

	public static Collection<Project> getAuthorizedProjects(ProjectCache cache, BasePermission permission) {
		return getAuthorizedProjects(getSubject(), cache, permission);
	}

	public static Collection<Project> getAuthorizedProjects(BasePermission permission) {
		return getAuthorizedProjects(getSubject(), permission);
	}
	
	private static SettingService getSettingService() {
		return OneDev.getInstance(SettingService.class);
	}

	public static Collection<Project> getAuthorizedProjects(Subject subject, BasePermission permission) {
		return getAuthorizedProjects(subject, null, permission);
	}

	public static Collection<Project> getAuthorizedProjects(Subject subject, @Nullable ProjectCache cache, BasePermission permission) {
		var projectService = OneDev.getInstance(ProjectService.class);
		String principal = (String) subject.getPrincipal();
		var user = getAuthUser(principal);
		var accessToken = getAccessToken(principal);
		if (permission.isApplicable(UserFacade.of(getUser(user, accessToken)))) {
			if (cache == null)
				cache = projectService.cloneCache();
			Collection<Long> authorizedProjectIds = new HashSet<>();
			if (user != null) {
				if (user.isRoot() || user.isSystem()) {
					return cache.getProjects();
				} else {
					for (Group group : user.getGroups()) {
						if (group.isAdministrator())
							return cache.getProjects();
						for (GroupAuthorization authorization : group.getAuthorizations()) {
							if (authorization.getRole().implies(permission))
								authorizedProjectIds.addAll(cache.getSubtreeIds(authorization.getProject().getId()));
						}
					}

					for (UserAuthorization authorization : user.getProjectAuthorizations()) {
						if (authorization.getRole().implies(permission))
							authorizedProjectIds.addAll(cache.getSubtreeIds(authorization.getProject().getId()));
					}
				}
			}
			if (accessToken != null) {
				for (var authorization : accessToken.getAuthorizations()) {
					if (authorization.getRole().implies(permission))
						authorizedProjectIds.addAll(cache.getSubtreeIds(authorization.getProject().getId()));
				}
			}
			if (!isAnonymous(principal) || getSettingService().getSecuritySetting().isEnableAnonymousAccess())
				addIdsPermittedByDefaultRole(cache, authorizedProjectIds, permission);
			
			return authorizedProjectIds.stream().map(projectService::load).collect(toSet());
		} else {
			return new HashSet<>();
		}
	}
	
	private static Collection<User> filterApplicableUsers(Collection<User> users, BasePermission permission) {
		return users.stream().filter(it -> permission.isApplicable(it.getFacade())).collect(toList());
	}

	public static Collection<User> getAuthorizedUsers(Project project, BasePermission permission) {
		var userService = OneDev.getInstance(UserService.class);
		UserCache cache = userService.cloneCache();

		Collection<User> authorizedUsers = Sets.newHashSet(userService.getRoot());

		for (Group group: OneDev.getInstance(GroupService.class).queryAdminstrator())
			authorizedUsers.addAll(group.getMembers());

		Project current = project;
		do {
			if (current.isPermittedByLoginUser(permission))
				return filterApplicableUsers(cache.getUsers(), permission);

			for (UserAuthorization authorization: current.getUserAuthorizations()) {
				if (authorization.getRole().implies(permission))
					authorizedUsers.add(authorization.getUser());
			}

			for (GroupAuthorization authorization: current.getGroupAuthorizations()) {
				if (authorization.getRole().implies(permission)) {
					authorizedUsers.addAll(authorization.getGroup().getMembers());
				}
			}
			current = current.getParent();
		} while (current != null);

		if (permission instanceof ConfidentialIssuePermission) {
			ConfidentialIssuePermission confidentialIssuePermission = (ConfidentialIssuePermission) permission;
			for (IssueAuthorization authorization: confidentialIssuePermission.getIssue().getAuthorizations())
				authorizedUsers.add(authorization.getUser());
		}

		return filterApplicableUsers(authorizedUsers, permission);
	}

	private static void populateAccessibleJobNames(Collection<String> accessibleJobNames,
											Collection<String> availableJobNames, Role role) {
		for (String jobName: availableJobNames) {
			if (role.implies(new JobPermission(jobName, new AccessBuild())))
				accessibleJobNames.add(jobName);
		}
	}

	public static Collection<String> getAccessibleJobNames(Project project, Collection<String> availableJobNames) {
		return getAccessibleJobNames(getSubject(), project, availableJobNames);
	}

	public static Collection<String> getAccessibleJobNames(Subject subject, Project project, Collection<String> availableJobNames) {
		Collection<String> accessibleJobNames = new HashSet<>();
		if (subject.isPermitted(new SystemAdministration())) {
			accessibleJobNames.addAll(availableJobNames);
		} else {
			String principal = (String) subject.getPrincipal();
			var user = getAuthUser(principal);
			if (user != null) {
				for (UserAuthorization authorization: user.getProjectAuthorizations()) {
					if (authorization.getProject().isSelfOrAncestorOf(project)) {
						populateAccessibleJobNames(accessibleJobNames, availableJobNames,
								authorization.getRole());
					}
				}

				for (Group group: user.getGroups()) {
					for (GroupAuthorization authorization: group.getAuthorizations()) {
						if (authorization.getProject().isSelfOrAncestorOf(project)) {
							populateAccessibleJobNames(accessibleJobNames, availableJobNames,
									authorization.getRole());
						}
					}
				}
			}
			
			var accessToken = getAccessToken(principal);
			if (accessToken != null) {
				for (var authorization: accessToken.getAuthorizations()) {
					if (authorization.getProject().isSelfOrAncestorOf(project)) {
						populateAccessibleJobNames(accessibleJobNames, availableJobNames,
								authorization.getRole());
					}
				}
			}

			if (!isAnonymous(principal) || getSettingService().getSecuritySetting().isEnableAnonymousAccess()) {
				Project current = project;
				do {
					for (var authorization: current.getBaseAuthorizations()) {
						populateAccessibleJobNames(accessibleJobNames, availableJobNames, authorization.getRole());
					}
					current = current.getParent();
				} while (current != null);
			}
		}
		return accessibleJobNames;
	}

	private static void populateAccessibleReportNames(Map<String, Collection<String>> accessibleReportNames,
											   Map<String, Collection<String>> availableReportNames, Project project, Role role) {
		for (Map.Entry<String, Collection<String>> entry: availableReportNames.entrySet()) {
			String jobName = entry.getKey();
			for (String reportName: entry.getValue()) {
				if (role.implies(new JobPermission(jobName, new AccessBuildReports(reportName)))) 
					accessibleReportNames.computeIfAbsent(jobName, k -> new HashSet<>()).add(reportName);
			}
		}
	}
	
	public static Map<String, Collection<String>> getAccessibleReportNames(Project project, Class<?> metricClass,
																		   Map<String, Collection<String>> availableReportNames) {
		Map<String, Collection<String>> accessibleReportNames = new HashMap<>();
		var subject = getSubject();
		if (subject.isPermitted(new SystemAdministration())) {
			for (Map.Entry<String, Collection<String>> entry: availableReportNames.entrySet())
				accessibleReportNames.put(entry.getKey(), new HashSet<>(entry.getValue()));
		} else {
			String principal = (String) subject.getPrincipal();
			var user = getAuthUser(principal);
			if (user != null) {
				for (UserAuthorization authorization: user.getProjectAuthorizations()) {
					if (project.equals(authorization.getProject())) {
						populateAccessibleReportNames(accessibleReportNames, availableReportNames,
								project, authorization.getRole());
					}
				}
				for (Group group: user.getGroups()) {
					for (GroupAuthorization authorization: group.getAuthorizations()) {
						if (project.equals(authorization.getProject())) {
							populateAccessibleReportNames(accessibleReportNames, availableReportNames,
									project, authorization.getRole());
						}
					}
				}
			}
			var accessToken = getAccessToken(principal);
			if (accessToken != null) {
				for (var authorization: accessToken.getAuthorizations()) {
					if (project.equals(authorization.getProject())) {
						populateAccessibleReportNames(accessibleReportNames, availableReportNames,
								project, authorization.getRole());
					}
				}
			}
			if (!isAnonymous(principal) || getSettingService().getSecuritySetting().isEnableAnonymousAccess()) {
				for (var authorization: project.getBaseAuthorizations()) {
					populateAccessibleReportNames(accessibleReportNames, availableReportNames,
							project, authorization.getRole());
				}
			}
		}
		return accessibleReportNames;
	}
	
}