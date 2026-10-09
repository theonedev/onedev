package io.onedev.server.web.page.project.setting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionService;
import io.onedev.server.exception.NoSubscriptionException;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;
import io.onedev.server.web.mapper.ProjectMapperUtils;
import io.onedev.server.web.page.project.setting.wiki.WikiSettingPage;

public class ProjectDefaultsTest {

	@Test
	public void projectDefaultsAccessChecksApplyToNewPages() {
		var project = new Project();
		project.setId(Project.DEFAULT_ID);
		var projects = mock(ProjectService.class);
		when(projects.load(Project.DEFAULT_ID)).thenReturn(project);
		var server = mock(OneDev.class);
		when(server.isReady()).thenReturn(true);
		try (var oneDev = mockStatic(OneDev.class); var security = mockStatic(SecurityUtils.class)) {
			var subscription = mock(SubscriptionService.class);
			oneDev.when(() -> OneDev.getInstance(SubscriptionService.class)).thenReturn(subscription);
			oneDev.when(OneDev::getInstance).thenReturn(server);
			oneDev.when(() -> OneDev.getInstance(ProjectService.class)).thenReturn(projects);
			security.when(SecurityUtils::isAdministrator).thenReturn(true);
			security.when(() -> SecurityUtils.canAccessProject(project)).thenReturn(true);
			when(subscription.isSubscriptionActive()).thenReturn(true);
			var tester = new WicketTester();
			try {
				var params = new PageParameters().add(ProjectMapperUtils.PARAM_PROJECT, Project.DEFAULT_NAME);
				var page = new WikiSettingPage(params);
				assertSame(project, page.getProject());

				when(subscription.isSubscriptionActive()).thenReturn(false);
				assertSame(project, page.getProject());
				assertThrows(NoSubscriptionException.class, () -> new WikiSettingPage(params));

				when(subscription.isSubscriptionActive()).thenReturn(true);
				security.when(SecurityUtils::isAdministrator).thenReturn(false);
				assertSame(project, page.getProject());
				assertThrows(UnauthorizedException.class, () -> new WikiSettingPage(params));
			} finally {
				tester.destroy();
			}
		}
	}

	@Test
	public void systemProjectSuggestionsDoNotAccessGitStorage() {
		var project = new Project();
		project.setId(Project.DEFAULT_ID);
		try (var oneDev = mockStatic(OneDev.class)) {
			assertNull(project.getDefaultBranch());
			assertTrue(project.getBranchRefs().isEmpty());
			assertTrue(project.getTagRefs().isEmpty());
			assertTrue(project.getJobNames().isEmpty());
			oneDev.verifyNoInteractions();
		}
	}
}
