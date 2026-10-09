package io.onedev.server.web.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.mock.MockWebRequest;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.service.ProjectService;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.setting.wiki.WikiSettingPage;

public class ProjectDefaultsMapperTest {

	@Test
	public void defaultsAndProjectEditorsUseSeparateRoutes() {
		var tester = new WicketTester();
		try (var oneDev = mockStatic(OneDev.class)) {
			var projects = mock(ProjectService.class);
			when(projects.getReservedNames()).thenReturn(List.of());
			oneDev.when(() -> OneDev.getInstance(ProjectService.class)).thenReturn(projects);
			var defaults = new ProjectDefaultsMapper("wiki", WikiSettingPage.class);
			var ordinary = new ProjectPageMapper("${project}/~settings/wiki", WikiSettingPage.class);
			var defaultParams = ProjectPage.paramsOf(Project.DEFAULT_ID);
			var defaultHandler = new BookmarkablePageRequestHandler(new PageProvider(WikiSettingPage.class, defaultParams));
			assertEquals("~administration/project-defaults/wiki", defaults.mapHandler(defaultHandler).toString());
			assertNull(ordinary.mapHandler(defaultHandler));

			var projectParams = ProjectPage.paramsOf("parent/child");
			var projectHandler = new BookmarkablePageRequestHandler(new PageProvider(WikiSettingPage.class, projectParams));
			assertEquals("parent/child/~settings/wiki", ordinary.mapHandler(projectHandler).toString());
			assertNull(defaults.mapHandler(projectHandler));

			for (var query : new String[] {"", "?project=other"}) {
				var request = new MockWebRequest(Url.parse("~administration/project-defaults/wiki" + query));
				var mapped = (RenderPageRequestHandler) defaults.mapRequest(request);
				assertEquals(WikiSettingPage.class, mapped.getPageClass());
				assertEquals(Project.DEFAULT_NAME, mapped.getPageParameters().get(ProjectMapperUtils.PARAM_PROJECT).toString());
			}
		} finally {
			tester.destroy();
		}
	}
}
