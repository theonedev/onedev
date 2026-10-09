package io.onedev.server.rest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.shiro.authz.UnauthorizedException;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.onedev.commons.loader.AppLoader;
import io.onedev.server.model.*;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.jackson.hibernate.HibernateObjectMapperModule;
import io.onedev.server.web.util.WicketUtils;

class ProjectReferenceSecurityTest {

	@Test
	void jsonProjectReferencesRejectDefaultsExceptAdministratorAuthorizations() throws Exception {
		var dao = mock(Dao.class);
		var defaults = new Project(); defaults.setId(Project.DEFAULT_ID);
		var ordinary = new Project(); ordinary.setId(1L);
		when(dao.load(Project.class, Project.DEFAULT_ID)).thenReturn(defaults);
		when(dao.load(Project.class, 1L)).thenReturn(ordinary);
		var mapper = JsonMapper.builder()
				.visibility(PropertyAccessor.ALL, Visibility.NONE)
				.visibility(PropertyAccessor.FIELD, Visibility.ANY)
				.addModule(new HibernateObjectMapperModule(dao)).build();
		try (var loader = mockStatic(AppLoader.class); var security = mockStatic(SecurityUtils.class);
				var wicket = mockStatic(WicketUtils.class)) {
			loader.when(() -> AppLoader.getInstance(Dao.class)).thenReturn(dao);
			wicket.when(WicketUtils::isSubscriptionActive).thenReturn(true);
			for (var type: new Class<?>[] {UserAuthorization.class, GroupAuthorization.class,
					BaseAuthorization.class, AccessTokenAuthorization.class}) {
				var error = assertThrows(JsonMappingException.class, () -> mapper.readValue("{\"projectId\":-1}", type));
				assertInstanceOf(UnauthorizedException.class, error.getCause());
			}
			verify(dao, never()).load(eq(Project.class), anyLong());
			security.when(SecurityUtils::isAdministrator).thenReturn(true);
			assertSame(defaults, mapper.readValue("{\"projectId\":-1}", UserAuthorization.class).getProject());
			assertSame(defaults, mapper.readValue("{\"projectId\":-1}", GroupAuthorization.class).getProject());
			assertSame(defaults, mapper.readValue("{\"projectId\":-1}", BaseAuthorization.class).getProject());
			assertSame(defaults, mapper.readValue("{\"projectId\":-1}", AccessTokenAuthorization.class).getProject());
			clearInvocations(dao);
			for (var type: new Class<?>[] {Iteration.class, ProjectLabel.class})
				assertThrows(JsonMappingException.class, () -> mapper.readValue("{\"projectId\":-1}", type));
			assertThrows(JsonMappingException.class, () -> mapper.readValue("{\"targetProjectId\":-1}", PullRequest.class));
			assertThrows(JsonMappingException.class, () -> mapper.readValue("{\"projectId\":-2}", UserAuthorization.class));
			verify(dao, never()).load(eq(Project.class), anyLong());
			security.when(SecurityUtils::isAdministrator).thenReturn(false);
			assertSame(ordinary, mapper.readValue("{\"projectId\":1}", Iteration.class).getProject());
		}
	}
}
