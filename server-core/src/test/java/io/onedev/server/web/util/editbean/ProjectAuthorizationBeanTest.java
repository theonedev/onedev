package io.onedev.server.web.util.editbean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;

import org.apache.wicket.model.Model;
import org.junit.jupiter.api.Test;

import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionService;
import io.onedev.server.exception.NoSubscriptionException;
import io.onedev.server.model.Project;
import io.onedev.server.service.ProjectService;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.web.component.project.choice.ProjectChoiceProvider;
import io.onedev.server.web.component.select2.Response;

public class ProjectAuthorizationBeanTest {

	@Test
	public void defaultProjectChoiceAndResolutionUseCurrentSubscription() {
		var project = new Project();
		project.setId(Project.DEFAULT_ID);
		project.setName(Project.DEFAULT_NAME);
		project.setPath(Project.DEFAULT_NAME);
		var service = mock(ProjectService.class);
		when(service.cloneCache()).thenReturn(new ProjectCache(new HashMap<>()));
		when(service.load(Project.DEFAULT_ID)).thenReturn(project);
		try (var oneDev = mockStatic(OneDev.class)) {
			var subscription = mock(SubscriptionService.class);
			oneDev.when(() -> OneDev.getInstance(SubscriptionService.class)).thenReturn(subscription);
			oneDev.when(() -> OneDev.getInstance(ProjectService.class)).thenReturn(service);
			var bean = new ProjectAuthorizationBean();
			bean.setProjectPath(Project.DEFAULT_NAME);
			assertEquals(List.of(), ReflectionUtils.invokeStaticMethod(ProjectAuthorizationBean.class, "getProjectChoices"));
			assertThrows(NoSubscriptionException.class, bean::resolveProject);
			verify(service, never()).load(Project.DEFAULT_ID);

			when(subscription.isSubscriptionActive()).thenReturn(true);
			assertEquals(List.of(project), ReflectionUtils.invokeStaticMethod(ProjectAuthorizationBean.class, "getProjectChoices"));
			assertSame(project, bean.resolveProject());
			verify(service, never()).findByPath(Project.DEFAULT_NAME);
			when(subscription.isSubscriptionActive()).thenReturn(false);
			assertEquals(List.of(), ReflectionUtils.invokeStaticMethod(ProjectAuthorizationBean.class, "getProjectChoices"));
			assertThrows(NoSubscriptionException.class, bean::resolveProject);
			when(subscription.isSubscriptionActive()).thenReturn(true);
			assertEquals(List.of(project), ReflectionUtils.invokeStaticMethod(ProjectAuthorizationBean.class, "getProjectChoices"));
			assertSame(project, bean.resolveProject());

			var response = new Response<Project>();
			new ProjectChoiceProvider(Model.ofList(List.of(project))).query("~default", 0, response);
			assertEquals(List.of(project), response.getResults());
		}
	}
}
