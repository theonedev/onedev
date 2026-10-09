package io.onedev.server.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionService;
import io.onedev.server.model.AccessToken;
import io.onedev.server.model.AccessTokenAuthorization;
import io.onedev.server.model.BaseAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLastActivityDate;
import io.onedev.server.model.Role;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.BaseAuthorizationService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.facade.ProjectCache;

class AccessTokenRevocationTest {

	private MockedStatic<OneDev> oneDev;
	private Subject previousSubject;
	private Subject tokenSubject;
	private AccessToken token;
	private Role role;
	private Project defaults;
	private Project root;
	private Project child;
	private Project other;
	private ProjectCache cache;
	private BaseAuthorizationService baseAuthorizations;
	private final Set<Long> managedProjectIds = new HashSet<>();
	private static final Map<String, Collection<String>> REPORTS = Map.of("build", List.of("coverage"));

	@BeforeEach
	void setUp() {
		previousSubject = ThreadContext.getSubject();
		oneDev = mockStatic(OneDev.class);
		var subscription = mock(SubscriptionService.class);
		oneDev.when(() -> OneDev.getInstance(SubscriptionService.class)).thenReturn(subscription);
		when(subscription.isSubscriptionActive()).thenReturn(true);
		var settings = mock(SettingService.class);
		oneDev.when(() -> OneDev.getInstance(SettingService.class)).thenReturn(settings);
		when(settings.getIssueSetting()).thenReturn(new GlobalIssueSetting());
		defaults = project(Project.DEFAULT_ID, Project.DEFAULT_NAME);
		root = project(1L, "root");
		child = project(2L, "root/child");
		child.setParent(root);
		other = project(3L, "other");
		var projects = mock(ProjectService.class);
		oneDev.when(() -> OneDev.getInstance(ProjectService.class)).thenReturn(projects);
		when(projects.load(Project.DEFAULT_ID)).thenReturn(defaults);
		cache = new ProjectCache(new HashMap<>());
		for (var project: List.of(root, child, other)) {
			cache.put(project.getId(), project.getFacade());
			when(projects.load(project.getId())).thenReturn(project);
		}
		baseAuthorizations = mock(BaseAuthorizationService.class);
		oneDev.when(() -> OneDev.getInstance(BaseAuthorizationService.class)).thenReturn(baseAuthorizations);
		var ownerSubject = mock(Subject.class);
		when(ownerSubject.isPermitted(any(Permission.class))).thenAnswer(it -> {
			var permission = (ProjectPermission) it.getArgument(0);
			return managedProjectIds.contains(permission.getProject().getId());
		});
		var owner = spy(new User());
		owner.setId(100L);
		owner.setName("owner");
		doReturn(ownerSubject).when(owner).asSubject();
		token = new AccessToken();
		token.setId(10L);
		token.setOwner(owner);
		var tokens = mock(AccessTokenService.class);
		oneDev.when(() -> OneDev.getInstance(AccessTokenService.class)).thenReturn(tokens);
		when(tokens.get(token.getId())).thenReturn(token);
		tokenSubject = mock(Subject.class);
		when(tokenSubject.getPrincipal()).thenReturn(SecurityUtils.asAccessTokenPrincipal(token.getId()));
		ThreadContext.bind(tokenSubject);
		role = new Role();
		role.setId(1L);
		role.setName("manager");
		role.setManageProject(true);
	}

	@AfterEach
	void tearDown() {
		ThreadContext.unbindSubject();
		if (previousSubject != null)
			ThreadContext.bind(previousSubject);
		oneDev.close();
	}

	private Project project(Long id, String path) {
		var project = new Project();
		project.setId(id);
		project.setName(path);
		project.setPath(path);
		project.setLastActivityDate(new ProjectLastActivityDate());
		return project;
	}

	private void grant(Project project) {
		var authorization = new AccessTokenAuthorization();
		authorization.setProject(project);
		authorization.setRole(role);
		authorization.setToken(token);
		token.getAuthorizations().add(authorization);
	}

	private void assertAccess(Project project, boolean expected) {
		assertEquals(expected, SecurityUtils.getAuthorizedProjects(tokenSubject, cache, new AccessProject()).contains(project),
				"project enumeration: " + project.getPath());
		assertEquals(expected, SecurityUtils.isAssignedRole(tokenSubject, project, role),
				"role assignment: " + project.getPath());
		var reports = SecurityUtils.getAccessibleReportNames(project, Object.class, REPORTS);
		assertEquals(expected, reports.containsKey("build"), "report access: " + project.getPath());
		if (expected)
			assertEquals(Set.of("coverage"), new HashSet<>(reports.get("build")));
	}

	@Test
	void revokingOwnerManagementInvalidatesSavedTokenGrants() {
		grant(root);
		managedProjectIds.add(root.getId());
		assertAccess(root, true);
		assertAccess(child, true);
		assertAccess(other, false);

		managedProjectIds.clear();
		assertAccess(root, false);
		assertAccess(child, false);
		assertEquals(1, token.getAuthorizations().size());

		managedProjectIds.add(root.getId());
		assertAccess(child, true);
	}

	@Test
	void managingChildDoesNotValidateARevokedParentGrant() {
		grant(root);
		managedProjectIds.add(child.getId());
		assertAccess(root, false);
		assertAccess(child, false);
	}

	@Test
	void revocationDoesNotDiscardOtherValidTokenGrants() {
		grant(root);
		grant(other);
		managedProjectIds.add(other.getId());
		assertAccess(root, false);
		assertAccess(child, false);
		assertAccess(other, true);
	}

	@Test
	void independentlyGrantedDefaultRoleRemainsEffective() {
		grant(root);
		var authorization = new BaseAuthorization();
		authorization.setProject(root);
		authorization.setRole(role);
		root.getBaseAuthorizations().add(authorization);
		role.getBaseAuthorizations().add(authorization);
		when(baseAuthorizations.query()).thenReturn(List.of(authorization));
		assertAccess(root, true);
		assertAccess(child, true);
		assertAccess(other, false);
	}

	@Test
	void legacyDefaultsTokenGrantAlsoRequiresOwnerManagement() {
		grant(defaults);
		managedProjectIds.add(defaults.getId());
		assertAccess(root, true);
		assertAccess(other, true);
		managedProjectIds.clear();
		assertAccess(root, false);
		assertAccess(other, false);
	}
}
