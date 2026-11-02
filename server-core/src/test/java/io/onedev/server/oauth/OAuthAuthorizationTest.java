package io.onedev.server.oauth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionService;
import io.onedev.server.model.*;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.model.support.role.CodePrivilege;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.security.DefaultAuthorizingService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.*;
import io.onedev.server.service.*;
import io.onedev.server.util.facade.ProjectCache;

class OAuthAuthorizationTest {

	private MockedStatic<OneDev> oneDev;
	private Subject previous;
	private Subject subject;
	private DefaultAuthorizingService realm;
	private AccessToken token;
	private User owner;
	private Project selected;
	private Project child;
	private Project other;
	private ProjectCache cache;
	private Role reader;
	private Role manager;
	private final List<ProjectPermission> ownerPermissions = new ArrayList<>();

	@BeforeEach
	void setUp() throws Exception {
		oneDev = mockStatic(OneDev.class);
		previous = ThreadContext.getSubject();
		var subscription = mock(SubscriptionService.class);
		oneDev.when(() -> OneDev.getInstance(SubscriptionService.class)).thenReturn(subscription);
		var settings = mock(SettingService.class);
		when(settings.getIssueSetting()).thenReturn(new GlobalIssueSetting());
		when(settings.getSecuritySetting()).thenReturn(new SecuritySetting());
		oneDev.when(() -> OneDev.getInstance(SettingService.class)).thenReturn(settings);
		var projects = mock(ProjectService.class);
		oneDev.when(() -> OneDev.getInstance(ProjectService.class)).thenReturn(projects);
		selected = project(1L, "selected");
		child = project(2L, "selected/child");
		child.setParent(selected);
		other = project(3L, "other");
		var defaults = project(Project.DEFAULT_ID, Project.DEFAULT_NAME);
		when(projects.load(Project.DEFAULT_ID)).thenReturn(defaults);
		cache = new ProjectCache(new HashMap<>());
		for (var project: List.of(selected, child, other)) {
			cache.put(project.getId(), project.getFacade());
			when(projects.load(project.getId())).thenReturn(project);
		}
		reader = new Role();
		reader.setId(1L);
		reader.setCodePrivilege(CodePrivilege.READ);
		manager = new Role();
		manager.setId(2L);
		manager.setManageProject(true);
		owner = spy(new User());
		owner.setId(100L);
		owner.setName("reader");
		var ownerSubject = mock(Subject.class);
		var ownerPrincipal = SecurityUtils.asUserPrincipal(owner.getId());
		when(ownerSubject.getPrincipal()).thenReturn(ownerPrincipal);
		when(ownerSubject.isPermitted(any(Permission.class))).thenAnswer(it -> ownerPermissions.stream().anyMatch(p -> p.implies(it.getArgument(0))));
		doReturn(ownerSubject).when(owner).asSubject();
		var users = mock(UserService.class);
		oneDev.when(() -> OneDev.getInstance(UserService.class)).thenReturn(users);
		when(users.get(owner.getId())).thenReturn(owner);
		var userAuthorization = new UserAuthorization();
		userAuthorization.setUser(owner);
		userAuthorization.setProject(selected);
		userAuthorization.setRole(manager);
		owner.getProjectAuthorizations().add(userAuthorization);
		ownerPermissions.add(new ProjectPermission(selected, manager));
		token = new AccessToken();
		token.setId(10L);
		token.setOwner(owner);
		token.setOauthGrant(new OAuthGrant());
		var authorization = new AccessTokenAuthorization();
		authorization.setToken(token);
		authorization.setProject(selected);
		authorization.setRole(manager);
		token.getAuthorizations().add(authorization);
		var tokens = mock(AccessTokenService.class);
		oneDev.when(() -> OneDev.getInstance(AccessTokenService.class)).thenReturn(tokens);
		when(tokens.get(token.getId())).thenReturn(token);
		var sessions = mock(SessionService.class);
		when(sessions.call(any())).thenAnswer(it -> ((Callable<?>) it.getArgument(0)).call());
		realm = new DefaultAuthorizingService();
		inject("sessionService", sessions);
		inject("settingService", settings);
		inject("subscriptionService", subscription);
		inject("issueAuthorizationService", mock(IssueAuthorizationService.class));
		subject = mock(Subject.class);
		when(subject.getPrincipal()).thenReturn(SecurityUtils.asAccessTokenPrincipal(token.getId()));
		when(subject.isPermitted(any(Permission.class))).thenAnswer(it -> realm.doGetAuthorizationInfo(SecurityUtils.asPrincipals((String) subject.getPrincipal()))
				.getObjectPermissions().stream().anyMatch(p -> p.implies(it.getArgument(0))));
		ThreadContext.bind(subject);
		var bases = mock(BaseAuthorizationService.class);
		oneDev.when(() -> OneDev.getInstance(BaseAuthorizationService.class)).thenReturn(bases);
	}

	private Project project(Long id, String path) {
		var project = new Project();
		project.setId(id); project.setName(path); project.setPath(path);
		project.setLastActivityDate(new ProjectLastActivityDate());
		return project;
	}

	private void inject(String name, Object value) throws Exception {
		var field = DefaultAuthorizingService.class.getDeclaredField(name);
		field.setAccessible(true); field.set(realm, value);
	}

	@AfterEach
	void tearDown() {
		ThreadContext.unbindSubject();
		if (previous != null) ThreadContext.bind(previous);
		oneDev.close();
	}

	@Test
	void existingGrantRequiresOwnerToManageAuthorizedProject() {
		assertTrue(SecurityUtils.canReadCode(subject, selected));
		assertTrue(SecurityUtils.canReadCode(subject, child));
		assertTrue(SecurityUtils.canManageProject(subject, selected));
		assertTrue(SecurityUtils.isAssignedRole(subject, selected, manager));
		ownerPermissions.clear();
		ownerPermissions.add(new ProjectPermission(selected, reader));
		assertFalse(SecurityUtils.canReadCode(subject, selected));
		assertFalse(SecurityUtils.canReadCode(subject, child));
		assertFalse(SecurityUtils.canWriteCode(subject, selected));
		assertFalse(SecurityUtils.canManageProject(subject, selected));
		assertFalse(SecurityUtils.isAssignedRole(subject, selected, manager));
		assertFalse(SecurityUtils.canReadCode(subject, other));
		assertTrue(SecurityUtils.getAuthorizedProjects(subject, cache, new ReadCode()).isEmpty());
	}

	@Test
	void consentRestrictsEvenAnOwnersBroaderProjectPermissions() {
		ownerPermissions.clear();
		ownerPermissions.add(new ProjectPermission(selected, manager));
		token.getAuthorizations().iterator().next().setRole(reader);
		assertTrue(SecurityUtils.canReadCode(subject, selected));
		assertTrue(SecurityUtils.isAssignedRole(subject, selected, reader));
		assertEquals(2, SecurityUtils.getAuthorizedProjects(subject, cache, new ReadCode()).size());
		assertFalse(SecurityUtils.canWriteCode(subject, selected));
		assertFalse(SecurityUtils.isAdministrator(subject));
	}

	@Test
	void defaultProjectRolesApplyOutsideConsent() {
		var base = new BaseAuthorization();
		base.setProject(other); base.setRole(manager);
		other.getBaseAuthorizations().add(base);
		manager.getBaseAuthorizations().add(base);
		when(OneDev.getInstance(BaseAuthorizationService.class).query()).thenReturn(List.of(base));
		token.getAuthorizations().clear();
		ownerPermissions.clear();
		assertTrue(SecurityUtils.canAccessProject(subject, other));
		assertTrue(SecurityUtils.isAssignedRole(subject, other, manager));
		assertEquals(Set.of(other), SecurityUtils.getAuthorizedProjects(subject, cache, new ReadCode()));
		assertEquals(Map.of("build", Set.of("coverage")), SecurityUtils.getAccessibleReportNames(other,
				Object.class, Map.of("build", List.of("coverage"))));
		assertFalse(SecurityUtils.canAccessProject(subject, selected));
	}

	@Test
	void newlyCheckedDelegationReflectsOwnerPermissionRemoval() {
		assertTrue(SecurityUtils.canReadCode(subject, selected));
		ownerPermissions.clear();
		assertFalse(SecurityUtils.canReadCode(subject, selected));
	}
}
