package io.onedev.server.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.cycle.RequestCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionService;
import io.onedev.server.model.AccessToken;
import io.onedev.server.model.AccessTokenAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLastActivityDate;
import io.onedev.server.model.Role;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.model.support.role.JobPrivilege;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.security.permission.ManageProject;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.SystemAdministration;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.BaseAuthorizationService;
import io.onedev.server.service.IssueAuthorizationService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.util.facade.ProjectCache;

class AuthorizationInfoCacheTest {

	private MockedStatic<OneDev> oneDev;
	private Subject previousSubject;
	private DefaultAuthorizingService authorizing;
	private DefaultSecurityManager securityManager;
	private IssueAuthorizationService issueAuthorizations;
	private UserService users;
	private AccessTokenService tokens;
	private SubscriptionService subscription;
	private User owner;
	private AccessToken token;
	private Subject tokenSubject;
	private Role role;
	private ProjectCache cache;
	private final List<Project> projects = new ArrayList<>();

	@BeforeEach
	void setUp() throws Exception {
		assertNull(AuthorizationInfoCache.get());
		assertNull(RequestCycle.get());
		previousSubject = ThreadContext.getSubject();
		oneDev = mockStatic(OneDev.class);
		subscription = mock(SubscriptionService.class);
		when(subscription.isSubscriptionActive()).thenReturn(true);
		oneDev.when(() -> OneDev.getInstance(SubscriptionService.class)).thenReturn(subscription);
		var settings = mock(SettingService.class);
		when(settings.getIssueSetting()).thenReturn(new GlobalIssueSetting());
		when(settings.getSecuritySetting()).thenReturn(new SecuritySetting());
		oneDev.when(() -> OneDev.getInstance(SettingService.class)).thenReturn(settings);
		var projectService = mock(ProjectService.class);
		oneDev.when(() -> OneDev.getInstance(ProjectService.class)).thenReturn(projectService);
		var defaults = project(Project.DEFAULT_ID);
		when(projectService.load(Project.DEFAULT_ID)).thenReturn(defaults);
		oneDev.when(() -> OneDev.getInstance(BaseAuthorizationService.class))
				.thenReturn(mock(BaseAuthorizationService.class));
		users = mock(UserService.class);
		oneDev.when(() -> OneDev.getInstance(UserService.class)).thenReturn(users);
		owner = spy(new User());
		owner.setId(100L);
		owner.setName("owner");
		when(users.get(owner.getId())).thenReturn(owner);
		role = new Role();
		role.setId(1L);
		role.setName("manager");
		role.setManageProject(true);
		grantOwner(defaults);
		token = new AccessToken();
		token.setId(1L);
		token.setOwner(owner);
		tokens = mock(AccessTokenService.class);
		when(tokens.get(token.getId())).thenReturn(token);
		oneDev.when(() -> OneDev.getInstance(AccessTokenService.class)).thenReturn(tokens);
		cache = new ProjectCache(new HashMap<>());
		for (long id = 1; id <= 100; id++) {
			var project = project(id);
			projects.add(project);
			cache.put(id, project.getFacade());
			when(projectService.load(id)).thenReturn(project);
			var authorization = new AccessTokenAuthorization();
			authorization.setToken(token);
			authorization.setProject(project);
			authorization.setRole(role);
			token.getAuthorizations().add(authorization);
		}
		var sessions = mock(SessionService.class);
		when(sessions.call(any())).thenAnswer(it -> ((Callable<?>) it.getArgument(0)).call());
		issueAuthorizations = mock(IssueAuthorizationService.class);
		authorizing = new DefaultAuthorizingService();
		inject("sessionService", sessions);
		inject("settingService", settings);
		inject("issueAuthorizationService", issueAuthorizations);
		securityManager = new DefaultSecurityManager(authorizing);
		var ownerSubject = new Subject.Builder(securityManager).principals(owner.getPrincipals()).buildSubject();
		doReturn(ownerSubject).when(owner).asSubject();
		tokenSubject = newTokenSubject();
		ThreadContext.bind(tokenSubject);
	}

	@AfterEach
	void tearDown() {
		ThreadContext.unbindSubject();
		if (previousSubject != null)
			ThreadContext.bind(previousSubject);
		if (securityManager != null)
			securityManager.destroy();
		if (oneDev != null)
			oneDev.close();
		assertNull(AuthorizationInfoCache.get());
	}

	private void inject(String name, Object value) throws Exception {
		var field = DefaultAuthorizingService.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(authorizing, value);
	}

	private Project project(Long id) {
		var project = new Project();
		project.setId(id);
		project.setName("project" + id);
		project.setPath(project.getName());
		project.setLastActivityDate(new ProjectLastActivityDate());
		return project;
	}

	private void grantOwner(Project project) {
		var authorization = new UserAuthorization();
		authorization.setProject(project);
		authorization.setUser(owner);
		authorization.setRole(role);
		owner.getProjectAuthorizations().add(authorization);
	}

	private Subject newTokenSubject() {
		return new Subject.Builder(securityManager)
				.principals(SecurityUtils.asPrincipals(SecurityUtils.asAccessTokenPrincipal(token.getId())))
				.buildSubject();
	}

	@Test
	void projectEnumerationLoadsOwnerOnceForOneHundredGrantsAndRefreshesNextOperation() {
		assertEquals(100, SecurityUtils.getAuthorizedProjects(tokenSubject, cache, new AccessProject()).size());
		verify(issueAuthorizations).getAuthorizedIssueIds(owner);
		assertNull(AuthorizationInfoCache.get());

		owner.getProjectAuthorizations().clear();
		assertTrue(SecurityUtils.getAuthorizedProjects(newTokenSubject(), cache, new AccessProject()).isEmpty());
		verify(issueAuthorizations, times(2)).getAuthorizedIssueIds(owner);
	}

	@Test
	void realmTokenAuthorizationAlsoLoadsOwnerOnceAndRefreshesForANewSubject() {
		var permission = new ProjectPermission(projects.get(0), new AccessProject());
		assertTrue(tokenSubject.isPermitted(permission));
		verify(issueAuthorizations).getAuthorizedIssueIds(owner);
		assertNull(AuthorizationInfoCache.get());

		owner.getProjectAuthorizations().clear();
		assertFalse(newTokenSubject().isPermitted(permission));
		verify(issueAuthorizations, times(2)).getAuthorizedIssueIds(owner);
	}

	@Test
	void roleLookupReusesOwnerInfoAcrossRejectedGrantsAndClosesOnEarlyReturn() {
		for (int i = 1; i < projects.size(); i++)
			projects.get(i).setParent(projects.get(i - 1));
		var leaf = projects.get(projects.size() - 1);
		owner.getProjectAuthorizations().clear();
		grantOwner(leaf);
		assertTrue(SecurityUtils.isAssignedRole(tokenSubject, leaf, role));
		verify(issueAuthorizations).getAuthorizedIssueIds(owner);
		assertNull(AuthorizationInfoCache.get());

		owner.getProjectAuthorizations().clear();
		assertFalse(SecurityUtils.isAssignedRole(newTokenSubject(), leaf, role));
		verify(issueAuthorizations, times(2)).getAuthorizedIssueIds(owner);
	}

	@Test
	void reportLookupSharesOwnerInfoBetweenAdministratorCheckAndGrantChecks() {
		Map<String, Collection<String>> reports = Map.of("build", List.of("coverage"));
		var accessible = SecurityUtils.getAccessibleReportNames(projects.get(0), Object.class, reports);
		assertEquals(Set.of("coverage"), accessible.get("build"));
		verify(issueAuthorizations).getAuthorizedIssueIds(owner);
		assertNull(AuthorizationInfoCache.get());

		owner.getProjectAuthorizations().clear();
		ThreadContext.bind(newTokenSubject());
		assertTrue(SecurityUtils.getAccessibleReportNames(projects.get(0), Object.class, reports).isEmpty());
		verify(issueAuthorizations, times(2)).getAuthorizedIssueIds(owner);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void mixedGrantsPreserveProjectAndPrivilegeBoundaries(boolean deniedGrantFirst) {
		var allowed = projects.get(0);
		var denied = projects.get(1);
		var child = projects.get(2);
		child.setParent(allowed);
		child.setPath(allowed.getPath() + "/" + child.getName());
		cache.put(child.getId(), child.getFacade());
		owner.getProjectAuthorizations().clear();
		grantOwner(allowed);
		var reader = new Role();
		reader.setId(2L);
		reader.setName("reader");
		var job = new JobPrivilege();
		job.setJobNames("build");
		job.setAccessibleReports("coverage");
		reader.getJobPrivileges().add(job);
		var grants = new ArrayList<>(token.getAuthorizations());
		var allowedGrant = grants.get(0);
		allowedGrant.setRole(reader);
		var deniedGrant = grants.get(1);
		token.setAuthorizations(deniedGrantFirst ? List.of(deniedGrant, allowedGrant) : List.of(allowedGrant, deniedGrant));

		assertEquals(Set.of(allowed, child),
				SecurityUtils.getAuthorizedProjects(tokenSubject, cache, new AccessProject()));
		assertTrue(SecurityUtils.getAuthorizedProjects(tokenSubject, cache, new ManageProject()).isEmpty());
		for (var project : List.of(allowed, child)) {
			assertTrue(tokenSubject.isPermitted(new ProjectPermission(project, new AccessProject())));
			assertFalse(SecurityUtils.canManageProject(tokenSubject, project));
			assertTrue(SecurityUtils.isAssignedRole(tokenSubject, project, reader));
			assertFalse(SecurityUtils.isAssignedRole(tokenSubject, project, role));
			assertEquals(Map.of("build", Set.of("coverage")), SecurityUtils.getAccessibleReportNames(project,
					Object.class, Map.of("build", List.of("coverage", "private"), "deploy", List.of("coverage"))));
		}
		for (var project : List.of(denied, projects.get(3))) {
			assertFalse(tokenSubject.isPermitted(new ProjectPermission(project, new AccessProject())));
			assertFalse(SecurityUtils.isAssignedRole(tokenSubject, project, role));
			assertTrue(SecurityUtils.getAccessibleReportNames(project, Object.class,
					Map.of("build", List.of("coverage"))).isEmpty());
		}
	}

	@Test
	void nestedOperationsKeepUsersTokensAndAnonymousPermissionsSeparate() {
		var grants = new ArrayList<>(token.getAuthorizations());
		token.setAuthorizations(List.of(grants.get(0)));
		var otherToken = new AccessToken();
		otherToken.setId(2L);
		otherToken.setOwner(owner);
		grants.get(1).setToken(otherToken);
		otherToken.setAuthorizations(List.of(grants.get(1)));
		when(tokens.get(otherToken.getId())).thenReturn(otherToken);
		var otherSubject = new Subject.Builder(securityManager).principals(otherToken.getPrincipals()).buildSubject();
		var root = new User();
		root.setId(User.ROOT_ID);
		root.setName("root");
		assertEquals(token.getId(), root.getId());
		when(users.get(root.getId())).thenReturn(root);
		var rootSubject = new Subject.Builder(securityManager).principals(root.getPrincipals()).buildSubject();
		var anonymous = new Subject.Builder(securityManager).principals(SecurityUtils.PRINCIPALS_ANONYMOUS).buildSubject();
		try (var ignored = AuthorizationInfoCache.open()) {
			assertTrue(rootSubject.isPermitted(new SystemAdministration()));
			assertFalse(tokenSubject.isPermitted(new SystemAdministration()));
			assertTrue(tokenSubject.isPermitted(new ProjectPermission(projects.get(0), new AccessProject())));
			assertFalse(tokenSubject.isPermitted(new ProjectPermission(projects.get(1), new AccessProject())));
			assertEquals(Set.of(projects.get(0)), SecurityUtils.getAuthorizedProjects(tokenSubject, cache, new AccessProject()));
			assertEquals(Set.of(projects.get(1)), SecurityUtils.getAuthorizedProjects(otherSubject, cache, new AccessProject()));
			assertFalse(otherSubject.isPermitted(new ProjectPermission(projects.get(0), new AccessProject())));
			assertTrue(otherSubject.isPermitted(new ProjectPermission(projects.get(1), new AccessProject())));
			assertFalse(anonymous.isPermitted(new SystemAdministration()));
			assertTrue(SecurityUtils.getAuthorizedProjects(anonymous, cache, new ManageProject()).isEmpty());
			assertSame(tokenSubject, ThreadContext.getSubject());
			verify(issueAuthorizations).getAuthorizedIssueIds(owner);
		}
	}

	@Test
	void ownerDisablementAndSubscriptionExpiryAreRespectedByNewSubjects() {
		var permission = new ProjectPermission(projects.get(0), new AccessProject());
		assertTrue(tokenSubject.isPermitted(permission));
		owner.setDisabled(true);
		assertFalse(newTokenSubject().isPermitted(permission));
		assertTrue(SecurityUtils.getAuthorizedProjects(newTokenSubject(), cache, new AccessProject()).isEmpty());
		owner.setDisabled(false);
		assertTrue(newTokenSubject().isPermitted(permission));
		when(subscription.isSubscriptionActive()).thenReturn(false);
		assertFalse(newTokenSubject().isPermitted(permission));
		assertTrue(SecurityUtils.getAuthorizedProjects(newTokenSubject(), cache, new AccessProject()).isEmpty());
	}

	@Test
	void newWicketRequestDoesNotReuseRevokedOwnerPermissions() {
		try (var requestCycles = mockStatic(RequestCycle.class)) {
			var firstRequest = requestCycle();
			requestCycles.when(RequestCycle::get).thenReturn(firstRequest);
			var permission = new ProjectPermission(projects.get(0), new AccessProject());
			assertTrue(tokenSubject.isPermitted(permission));
			assertEquals(100, SecurityUtils.getAuthorizedProjects(tokenSubject, cache, new AccessProject()).size());
			verify(issueAuthorizations).getAuthorizedIssueIds(owner);
			owner.getProjectAuthorizations().clear();
			var nextRequest = requestCycle();
			requestCycles.when(RequestCycle::get).thenReturn(nextRequest);
			var newSubject = newTokenSubject();
			assertFalse(newSubject.isPermitted(permission));
			assertTrue(SecurityUtils.getAuthorizedProjects(newSubject, cache, new AccessProject()).isEmpty());
			verify(issueAuthorizations, times(2)).getAuthorizedIssueIds(owner);
		}
	}

	private RequestCycle requestCycle() {
		var requestCycle = mock(RequestCycle.class);
		var metadata = new HashMap<MetaDataKey<?>, Object>();
		when(requestCycle.getMetaData(any())).thenAnswer(it -> metadata.get(it.getArgument(0)));
		doAnswer(it -> {
			metadata.put(it.getArgument(0), it.getArgument(1));
			return null;
		}).when(requestCycle).setMetaData(any(), any());
		return requestCycle;
	}

	@Test
	void failureDuringTokenAuthorizationDoesNotLeakTheScope() {
		var failure = new IllegalStateException("authorization lookup failed");
		when(issueAuthorizations.getAuthorizedIssueIds(owner)).thenThrow(failure);
		assertSame(failure, assertThrows(IllegalStateException.class,
				() -> SecurityUtils.getAuthorizedProjects(tokenSubject, cache, new AccessProject())));
		assertNull(AuthorizationInfoCache.get());

		doReturn(Set.of()).when(issueAuthorizations).getAuthorizedIssueIds(owner);
		assertEquals(100, SecurityUtils.getAuthorizedProjects(tokenSubject, cache, new AccessProject()).size());
		verify(issueAuthorizations, times(2)).getAuthorizedIssueIds(owner);
	}

	@Test
	void nestedFailureKeepsOuterScopeAndSeparatesPrincipals() {
		var otherUser = new User();
		otherUser.setId(101L);
		otherUser.setName("other");
		when(users.get(otherUser.getId())).thenReturn(otherUser);
		AuthorizationInfo ownerInfo;
		try (var outer = AuthorizationInfoCache.open()) {
			ownerInfo = authorizing.doGetAuthorizationInfo(owner.getPrincipals());
			assertThrows(IllegalStateException.class, () -> {
				try (var inner = AuthorizationInfoCache.open()) {
					assertSame(ownerInfo, authorizing.doGetAuthorizationInfo(owner.getPrincipals()));
					assertNotSame(ownerInfo, authorizing.doGetAuthorizationInfo(otherUser.getPrincipals()));
					throw new IllegalStateException("nested failure");
				}
			});
			assertSame(ownerInfo, authorizing.doGetAuthorizationInfo(owner.getPrincipals()));
			verify(issueAuthorizations).getAuthorizedIssueIds(owner);
			verify(issueAuthorizations).getAuthorizedIssueIds(otherUser);
		}
		assertNull(AuthorizationInfoCache.get());
		assertNotSame(ownerInfo, authorizing.doGetAuthorizationInfo(owner.getPrincipals()));
		verify(issueAuthorizations, times(2)).getAuthorizedIssueIds(owner);
	}

	@Test
	void scopeIsNotSharedWithOtherThreads() {
		try (var outer = AuthorizationInfoCache.open()) {
			var outerCache = AuthorizationInfoCache.get();
			CompletableFuture.runAsync(() -> {
				assertNull(AuthorizationInfoCache.get());
				try (var inner = AuthorizationInfoCache.open()) {
					assertNotSame(outerCache, AuthorizationInfoCache.get());
				}
				assertNull(AuthorizationInfoCache.get());
			}).join();
			assertSame(outerCache, AuthorizationInfoCache.get());
		}
	}

	@Test
	void wicketRequestCacheStillOutlivesIndividualOperations() {
		try (var requestCycles = mockStatic(RequestCycle.class)) {
			var requestCycle = mock(RequestCycle.class);
			var requestCache = new HashMap<String, AuthorizationInfo>();
			requestCycles.when(RequestCycle::get).thenReturn(requestCycle);
			when(requestCycle.getMetaData(any())).thenAnswer(it -> requestCache);
			AuthorizationInfo info;
			try (var first = AuthorizationInfoCache.open()) {
				info = authorizing.doGetAuthorizationInfo(owner.getPrincipals());
			}
			try (var second = AuthorizationInfoCache.open()) {
				assertSame(info, authorizing.doGetAuthorizationInfo(owner.getPrincipals()));
			}
			verify(issueAuthorizations).getAuthorizedIssueIds(owner);
		}
	}

}
