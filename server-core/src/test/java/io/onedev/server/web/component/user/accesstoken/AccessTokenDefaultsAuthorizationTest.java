package io.onedev.server.web.component.user.accesstoken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionService;
import io.onedev.server.exception.NoSubscriptionException;
import io.onedev.server.exception.NotAcceptableException;
import io.onedev.server.model.AccessToken;
import io.onedev.server.model.AccessTokenAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.rest.resource.AccessTokenAuthorizationResource;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ManageProject;
import io.onedev.server.service.AccessTokenAuthorizationService;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.util.HierarchicalContext;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.util.UserAware;

class AccessTokenDefaultsAuthorizationTest {

	private MockedStatic<OneDev> oneDev;
	private MockedStatic<SecurityUtils> security;
	private SubscriptionService subscription;
	private ProjectService projects;
	private Project defaults;
	private Project ordinary;
	private User owner;
	private Subject ownerSubject;

	@BeforeEach
	void setUp() {
		oneDev = mockStatic(OneDev.class);
		security = mockStatic(SecurityUtils.class);
		subscription = mock(SubscriptionService.class);
		oneDev.when(() -> OneDev.getInstance(SubscriptionService.class)).thenReturn(subscription);
		projects = mock(ProjectService.class);
		oneDev.when(() -> OneDev.getInstance(ProjectService.class)).thenReturn(projects);
		defaults = new Project();
		defaults.setId(Project.DEFAULT_ID);
		defaults.setName(Project.DEFAULT_NAME);
		defaults.setPath(Project.DEFAULT_NAME);
		ordinary = new Project();
		ordinary.setId(1L);
		ordinary.setPath("ordinary");
		when(projects.load(Project.DEFAULT_ID)).thenReturn(defaults);
		when(projects.findByPath("ordinary")).thenReturn(ordinary);
		owner = mock(User.class);
		ownerSubject = mock(Subject.class);
		when(owner.asSubject()).thenReturn(ownerSubject);
		security.when(SecurityUtils::getAuthUser).thenReturn(owner);
		security.when(() -> SecurityUtils.canManageProject(ownerSubject, defaults)).thenReturn(true);
		security.when(() -> SecurityUtils.canManageProject(ownerSubject, ordinary)).thenReturn(true);
	}

	@AfterEach
	void tearDown() {
		security.close();
		oneDev.close();
	}

	@Test
	void choiceAndDescriptionUseCurrentSubscriptionAndOwnerPermissions() {
		var context = mock(HierarchicalContext.class);
		var userAware = mock(UserAware.class);
		when(context.findData(UserAware.class)).thenReturn(userAware);
		when(userAware.getUser()).thenReturn(owner);
		security.when(() -> SecurityUtils.getAuthorizedProjects(eq(ownerSubject), any(ManageProject.class)))
				.thenReturn(List.of(ordinary));
		HierarchicalContext.push(context);
		try {
			when(subscription.isSubscriptionActive()).thenReturn(true);
			security.when(SecurityUtils::isAdministrator).thenReturn(true);
			assertEquals(List.of(ordinary), choices());
			assertEquals("", description());
			verify(projects, never()).load(Project.DEFAULT_ID);
			security.when(() -> SecurityUtils.isAdministrator(ownerSubject)).thenReturn(true);
			assertEquals(List.of(defaults, ordinary), choices());
			assertTrue(description().contains("~default"));
			security.when(SecurityUtils::isAdministrator).thenReturn(false);
			assertEquals(List.of(defaults, ordinary), choices());
			assertTrue(description().contains("~default"));
			when(subscription.isSubscriptionActive()).thenReturn(false);
			assertEquals(List.of(ordinary), choices());
			assertEquals("", description());
			when(subscription.isSubscriptionActive()).thenReturn(true);
			assertEquals(List.of(defaults, ordinary), choices());
			assertTrue(description().contains("~default"));
		} finally {
			HierarchicalContext.pop();
		}
	}

	private Object choices() {
		return ReflectionUtils.invokeStaticMethod(AccessTokenAuthorizationBean.class, "getManageableProjects");
	}

	private String description() {
		return (String) ReflectionUtils.invokeStaticMethod(AccessTokenAuthorizationBean.class, "getProjectDescription");
	}

	@Test
	void resolvingDefaultChoiceUsesCurrentSubscriptionAndChecksOwnerPermission() {
		var bean = new AccessTokenAuthorizationBean();
		bean.setProjectPath(Project.DEFAULT_NAME);
		when(subscription.isSubscriptionActive()).thenReturn(true);
		security.when(SecurityUtils::isAdministrator).thenReturn(true);
		assertThrows(UnauthorizedException.class, () -> bean.resolveProject(owner));
		verify(projects, never()).load(Project.DEFAULT_ID);
		security.when(() -> SecurityUtils.isAdministrator(ownerSubject)).thenReturn(true);
		assertSame(defaults, bean.resolveProject(owner));
		verify(projects, never()).findByPath(Project.DEFAULT_NAME);
		when(subscription.isSubscriptionActive()).thenReturn(false);
		assertThrows(NoSubscriptionException.class, () -> bean.resolveProject(owner));
		when(subscription.isSubscriptionActive()).thenReturn(true);
		assertSame(defaults, bean.resolveProject(owner));
		security.when(() -> SecurityUtils.canManageProject(ownerSubject, defaults)).thenReturn(false);
		assertThrows(UnauthorizedException.class, () -> bean.resolveProject(owner));

		security.when(SecurityUtils::isAdministrator).thenReturn(false);
		when(subscription.isSubscriptionActive()).thenReturn(false);
		bean.setProjectPath("ordinary");
		assertSame(ordinary, bean.resolveProject(owner));
		security.when(() -> SecurityUtils.canManageProject(ownerSubject, ordinary)).thenReturn(false);
		assertThrows(UnauthorizedException.class, () -> bean.resolveProject(owner));
	}

	@Test
	void restCreateAndUpdateEnforceTheSameDefaultScopeRules() {
		var service = mock(AccessTokenAuthorizationService.class);
		var resource = new AccessTokenAuthorizationResource(service, mock(AuditService.class));
		var token = new AccessToken();
		token.setOwner(owner);
		var authorization = new AccessTokenAuthorization();
		authorization.setId(5L);
		authorization.setToken(token);
		authorization.setProject(defaults);
		when(subscription.isSubscriptionActive()).thenReturn(true);
		assertThrows(UnauthorizedException.class, () -> resource.createAuthorization(authorization));
		assertThrows(UnauthorizedException.class, () -> resource.updateAuthorization(5L, authorization));
		verifyNoInteractions(service);

		security.when(SecurityUtils::isAdministrator).thenReturn(true);
		assertThrows(BadRequestException.class, () -> resource.createAuthorization(authorization));
		assertThrows(NotAcceptableException.class, () -> resource.updateAuthorization(5L, authorization));
		verifyNoInteractions(service);
		security.when(() -> SecurityUtils.isAdministrator(ownerSubject)).thenReturn(true);
		assertEquals(5L, resource.createAuthorization(authorization));
		assertEquals(200, resource.updateAuthorization(5L, authorization).getStatus());
		verify(service, times(2)).createOrUpdate(authorization);
		clearInvocations(service);

		when(subscription.isSubscriptionActive()).thenReturn(false);
		assertThrows(NoSubscriptionException.class, () -> resource.createAuthorization(authorization));
		assertThrows(NoSubscriptionException.class, () -> resource.updateAuthorization(5L, authorization));
		when(subscription.isSubscriptionActive()).thenReturn(true);
		security.when(() -> SecurityUtils.canManageProject(ownerSubject, defaults)).thenReturn(false);
		assertThrows(BadRequestException.class, () -> resource.createAuthorization(authorization));
		assertThrows(NotAcceptableException.class, () -> resource.updateAuthorization(5L, authorization));
		defaults.setId(-2L);
		assertThrows(NotFoundException.class, () -> resource.createAuthorization(authorization));
		assertThrows(NotFoundException.class, () -> resource.updateAuthorization(5L, authorization));
		verifyNoInteractions(service);
	}
}
