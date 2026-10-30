package io.onedev.server.rest.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.glassfish.jersey.model.Parameter.Source;
import org.glassfish.jersey.server.spi.internal.ParamValueFactoryWithSource;
import org.glassfish.jersey.server.spi.internal.ParameterValueHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.XStream;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.ImplementationRegistry;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.Group;
import io.onedev.server.model.Membership;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.SecuritySetting;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.rest.resource.GroupResource.GroupData;
import io.onedev.server.security.DefaultAuthorizingService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.GroupService;
import io.onedev.server.service.IssueAuthorizationService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.util.jackson.ObjectMapperProvider;
import io.onedev.server.util.jackson.hibernate.HibernateObjectMapperModule;
import io.onedev.server.validation.HibernateValidationTestSupport;

class GroupResourceTest extends HibernateValidationTestSupport {

	private Dao dao;
	private GroupService groups;
	private AuditService audit;
	private GroupResource resource;
	private Group group;
	private ObjectMapper mapper;
	private DefaultSecurityManager securityManager;
	private Subject previousSubject;
	private MockedStatic<AppLoader> loader;

	@BeforeEach
	void setUp() throws Exception {
		previousSubject = ThreadContext.getSubject();
		loader = mockStatic(AppLoader.class);
		loader.when(() -> AppLoader.getInstance(XStream.class)).thenReturn(new XStream());
		dao = mock(Dao.class);
		groups = mock(GroupService.class);
		audit = mock(AuditService.class);
		resource = new GroupResource(groups, audit);
		group = new Group();
		group.setId(7L);
		group.setName("team");
		group.setDescription("original");
		group.setCreateRootProjects(true);
		group.setEnforce2FA(true);
		when(dao.load(Group.class, 7L)).thenReturn(group);
		when(groups.load(7L)).thenReturn(group);
		mapper = new ObjectMapperProvider(Set.of(), Set.of(), mock(ImplementationRegistry.class),
				Set.of(it -> it.registerModule(new HibernateObjectMapperModule(dao)))).get();

		var member = new User();
		member.setId(2L);
		member.setName("member");
		var membership = new Membership();
		membership.setUser(member);
		membership.setGroup(group);
		member.getMemberships().add(membership);
		var root = new User();
		root.setId(User.ROOT_ID);
		root.setName("root");
		var users = mock(UserService.class);
		when(users.get(2L)).thenReturn(member);
		when(users.get(User.ROOT_ID)).thenReturn(root);
		loader.when(() -> AppLoader.getInstance(UserService.class)).thenReturn(users);
		var sessions = mock(SessionService.class);
		when(sessions.call(any())).thenAnswer(it -> ((Callable<?>) it.getArgument(0)).call());
		var settings = mock(SettingService.class);
		when(settings.getSecuritySetting()).thenReturn(new SecuritySetting());
		var realm = new DefaultAuthorizingService();
		inject(realm, "sessionService", sessions);
		inject(realm, "settingService", settings);
		inject(realm, "issueAuthorizationService", mock(IssueAuthorizationService.class));
		securityManager = new DefaultSecurityManager(realm);
		bindUser(2L);
	}

	@AfterEach
	void tearDown() {
		ThreadContext.unbindSubject();
		if (previousSubject != null)
			ThreadContext.bind(previousSubject);
		if (securityManager != null)
			securityManager.destroy();
		if (loader != null)
			loader.close();
	}

	private void inject(Object target, String name, Object value) throws Exception {
		var field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	private void bindUser(Long userId) {
		ThreadContext.bind(new Subject.Builder(securityManager)
				.principals(SecurityUtils.asPrincipals(SecurityUtils.asUserPrincipal(userId)))
				.authenticated(true).buildSubject());
	}

	private Method method(String name) {
		return Arrays.stream(GroupResource.class.getMethods()).filter(it -> it.getName().equals(name)).findFirst().orElseThrow();
	}

	private Object[] parameters(String methodName, String json) {
		var method = method(methodName);
		var body = new ParamValueFactoryWithSource<>(request -> {
			try {
				return mapper.readValue(json, method.getParameterTypes()[method.getParameterCount() - 1]);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}, Source.ENTITY);
		if (method.getParameterCount() == 2) {
			return ParameterValueHelper.getParameterValues(List.of(
					new ParamValueFactoryWithSource<>(request -> 7L, Source.PATH), body), null);
		}
		return ParameterValueHelper.getParameterValues(List.of(body), null);
	}

	@ParameterizedTest
	@ValueSource(strings = {"{\"administrator\":true}",
			"{\"name\":\"promoted\",\"administrator\":true,\"createRootProjects\":true,\"enforce2FA\":false}"})
	void groupMemberCannotPromoteTheirGroupDuringRequestDeserialization(String json) {
		assertFalse(SecurityUtils.isAdministrator());
		var args = parameters("updateGroup", json);
		assertFalse(group.isAdministrator());
		assertFalse(SecurityUtils.isAdministrator());
		var error = assertThrows(InvocationTargetException.class, () -> method("updateGroup").invoke(resource, args));
		assertInstanceOf(UnauthorizedException.class, error.getCause());
		assertEquals("team", group.getName());
		assertEquals("original", group.getDescription());
		assertTrue(group.isEnforce2FA());
		verifyNoInteractions(dao, groups, audit);
	}

	@Test
	void administratorUpdatesThePathGroupAndAuditsTheOriginalValues() throws Exception {
		bindUser(User.ROOT_ID);
		var oldContent = VersionedXmlDoc.fromBean(GroupData.from(group)).toXML();
		var args = parameters("updateGroup", "{\"id\":99,\"name\":\"renamed\",\"description\":null,"
				+ "\"administrator\":true,\"createRootProjects\":false,\"enforce2FA\":false}");
		assertTrue(validator.forExecutables().validateParameters(resource, method("updateGroup"), args).isEmpty());
		assertFalse(group.isAdministrator());
		verifyNoInteractions(dao, groups);
		method("updateGroup").invoke(resource, args);
		assertEquals(7L, group.getId());
		assertEquals("renamed", group.getName());
		assertNull(group.getDescription());
		assertTrue(group.isAdministrator());
		assertFalse(group.isCreateRootProjects());
		assertFalse(group.isEnforce2FA());
		verify(groups).update(group, "team");
		verify(audit).audit(null, "changed group \"renamed\" via RESTful API", oldContent,
				VersionedXmlDoc.fromBean(GroupData.from(group)).toXML());
		assertEquals(mapper.readTree(mapper.writeValueAsString(group)),
				mapper.readTree(mapper.writeValueAsString(resource.getGroup(7L))));
	}

	@Test
	void administratorCanCreateAGroupAndMembersCannot() throws Exception {
		var args = parameters("createGroup", "{\"id\":99,\"name\":\"new group\",\"administrator\":true}");
		var error = assertThrows(InvocationTargetException.class, () -> method("createGroup").invoke(resource, args));
		assertInstanceOf(UnauthorizedException.class, error.getCause());
		verifyNoInteractions(groups, audit);
		bindUser(User.ROOT_ID);
		doAnswer(it -> {
			Group created = it.getArgument(0);
			assertNull(created.getId());
			assertEquals("new group", created.getName());
			assertTrue(created.isAdministrator());
			assertFalse(created.isCreateRootProjects());
			assertFalse(created.isEnforce2FA());
			created.setId(8L);
			return null;
		}).when(groups).create(any());
		assertEquals(8L, method("createGroup").invoke(resource, args));
		verify(groups).create(any());
	}

	@ParameterizedTest
	@ValueSource(strings = {"createGroup", "updateGroup"})
	void requestValidationRequiresAGroupName(String methodName) {
		for (var json: List.of("{}", "{\"name\":\"\"}", "{\"name\":null}")) {
			assertFalse(validator.forExecutables().validateParameters(resource, method(methodName),
					parameters(methodName, json)).isEmpty());
		}
		assertTrue(validator.forExecutables().validateParameters(resource, method(methodName),
				parameters(methodName, "{\"name\":\"valid\"}")).isEmpty());
		verifyNoInteractions(dao, groups, audit);
	}
}
