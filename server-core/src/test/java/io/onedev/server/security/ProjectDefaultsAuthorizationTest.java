package io.onedev.server.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.shiro.subject.Subject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionService;
import io.onedev.server.model.*;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.service.*;
import io.onedev.server.util.facade.ProjectCache;

public class ProjectDefaultsAuthorizationTest {

	private MockedStatic<OneDev> oneDev;
	private SubscriptionService subscription;
	private Project defaults;
	private Project root;
	private Project child;
	private Project otherRoot;
	private User user;
	private Role role;
	private Subject subject;
	private ProjectCache cache;
	private BaseAuthorizationService baseAuthorizations;
	private DefaultAuthorizingService authorizing;

	@BeforeEach
	void setUp() throws Exception {
		oneDev = mockStatic(OneDev.class);
		subscription = mock(SubscriptionService.class);
		oneDev.when(() -> OneDev.getInstance(SubscriptionService.class)).thenReturn(subscription);
		when(subscription.isSubscriptionActive()).thenReturn(true);
		defaults = project(Project.DEFAULT_ID, Project.DEFAULT_NAME);
		root = project(1L, "root");
		child = project(2L, "root/child");
		child.setParent(root);
		otherRoot = project(3L, "other");
		var projects = mock(ProjectService.class);
		oneDev.when(() -> OneDev.getInstance(ProjectService.class)).thenReturn(projects);
		when(projects.load(Project.DEFAULT_ID)).thenReturn(defaults);
		cache = new ProjectCache(new HashMap<>());
		for (var project : List.of(root, child, otherRoot)) {
			cache.put(project.getId(), project.getFacade());
			when(projects.load(project.getId())).thenReturn(project);
		}
		baseAuthorizations = mock(BaseAuthorizationService.class);
		oneDev.when(() -> OneDev.getInstance(BaseAuthorizationService.class)).thenReturn(baseAuthorizations);
		user = new User();
		user.setId(100L);
		user.setName("tester");
		var users = mock(UserService.class);
		oneDev.when(() -> OneDev.getInstance(UserService.class)).thenReturn(users);
		when(users.get(user.getId())).thenReturn(user);
		role = new Role();
		role.setId(1L);
		role.setName("reader");
		subject = mock(Subject.class);
		when(subject.getPrincipal()).thenReturn(SecurityUtils.asUserPrincipal(user.getId()));
		var sessions = mock(SessionService.class);
		when(sessions.call(any())).thenAnswer(it -> ((Callable<?>) it.getArgument(0)).call());
		authorizing = new DefaultAuthorizingService();
		inject("sessionService", sessions);
		var settings = mock(SettingService.class);
		when(settings.getIssueSetting()).thenReturn(new io.onedev.server.model.support.administration.GlobalIssueSetting());
		oneDev.when(() -> OneDev.getInstance(SettingService.class)).thenReturn(settings);
		inject("settingService", settings);
		inject("issueAuthorizationService", mock(IssueAuthorizationService.class));
	}

	private void inject(String name, Object value) throws Exception {
		var field = DefaultAuthorizingService.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(authorizing, value);
	}

	private Project project(Long id, String path) {
		var project = new Project();
		project.setId(id);
		project.setName(path);
		project.setPath(path);
		project.setLastActivityDate(new ProjectLastActivityDate());
		return project;
	}

	@AfterEach
	void tearDown() {
		oneDev.close();
	}

	private void assertDefaultGrant(boolean expected) throws Exception {
		var method = DefaultAuthorizingService.class.getDeclaredMethod("newAuthorizationInfo", String.class);
		method.setAccessible(true);
		var info = (org.apache.shiro.authz.AuthorizationInfo) method.invoke(authorizing, subject.getPrincipal());
		for (var project : List.of(root, child, otherRoot)) {
			assertEquals(expected, info.getObjectPermissions().stream()
					.anyMatch(it -> it.implies(new ProjectPermission(project, new AccessProject()))));
			assertEquals(expected, SecurityUtils.isAssignedRole(subject, project, role));
		}
		var visible = SecurityUtils.getAuthorizedProjects(subject, cache, new AccessProject());
		assertEquals(expected ? 3 : 0, visible.size());
		assertFalse(visible.contains(defaults));
	}

	@Test
	void defaultUserAuthorizationControlsPermissionsAndProjectVisibility() throws Exception {
		var authorization = new UserAuthorization();
		authorization.setProject(defaults);
		authorization.setUser(user);
		authorization.setRole(role);
		user.getProjectAuthorizations().add(authorization);
		assertDefaultGrant(true);
		when(subscription.isSubscriptionActive()).thenReturn(false);
		assertDefaultGrant(false);
	}

	@Test
	void defaultGroupAuthorizationControlsPermissionsAndProjectVisibility() throws Exception {
		var group = new Group();
		group.setId(1L);
		var authorization = new GroupAuthorization();
		authorization.setProject(defaults);
		authorization.setGroup(group);
		authorization.setRole(role);
		group.getAuthorizations().add(authorization);
		var membership = new Membership();
		membership.setGroup(group);
		membership.setUser(user);
		user.getMemberships().add(membership);
		assertDefaultGrant(true);
		when(subscription.isSubscriptionActive()).thenReturn(false);
		assertDefaultGrant(false);
	}

	@Test
	void defaultRolesControlPermissionsAndProjectVisibility() throws Exception {
		var authorization = new BaseAuthorization();
		authorization.setProject(defaults);
		authorization.setRole(role);
		defaults.getBaseAuthorizations().add(authorization);
		role.getBaseAuthorizations().add(authorization);
		when(baseAuthorizations.query()).thenReturn(List.of(authorization));
		assertDefaultGrant(true);
		when(subscription.isSubscriptionActive()).thenReturn(false);
		assertDefaultGrant(false);
	}

	@Test
	void ordinaryGrantsStayWithinTheirProjectTree() {
		var grant = new ProjectPermission(root, new AccessProject());
		assertTrue(grant.implies(new ProjectPermission(child, new AccessProject())));
		assertFalse(grant.implies(new ProjectPermission(otherRoot, new AccessProject())));
		assertFalse(grant.implies(new ProjectPermission(defaults, new AccessProject())));
	}
}
