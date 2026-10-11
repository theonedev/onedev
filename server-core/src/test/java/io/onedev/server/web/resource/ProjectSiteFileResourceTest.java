package io.onedev.server.web.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.wicket.mock.MockWebRequest;
import org.apache.wicket.mock.MockWebResponse;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.model.Project;
import io.onedev.server.security.permission.AccessSites;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.impl.DefaultProjectService;
import io.onedev.server.util.SiteSyncUtils;
import io.onedev.server.util.artifact.DirectoryInfo;
import io.onedev.server.util.artifact.FileInfo;
import io.onedev.server.web.mapper.ProjectResourceMapper;

class ProjectSiteFileResourceTest {

	@Test
	void rejectsCaseAliasesOfExcludedSites(@TempDir Path sitesDir) throws Exception {
		for (var site : List.of("public", "secret", "TeamDocs")) {
			Files.createDirectory(sitesDir.resolve(site));
			Files.writeString(sitesDir.resolve(site).resolve("index.html"), "site content");
		}
		var tester = new WicketTester();
		var subject = mock(Subject.class);
		when(subject.isPermitted(any(Permission.class))).thenAnswer(it ->
				new AccessSites("* -secret").implies(((ProjectPermission) it.getArgument(0)).getPrivilege()));
		ThreadContext.bind(subject);
		try (var oneDev = mockStatic(OneDev.class)) {
			var projects = spy(new DefaultProjectService(Set.of()));
			doReturn(sitesDir.toFile()).when(projects).getSitesDir(1L);
			doAnswer(it -> ((ClusterTask<?>) it.getArgument(1)).call())
					.when(projects).runOnActiveServer(eq(1L), any());
			var project = new Project();
			project.setId(1L);
			project.setPath("test");
			doReturn(project).when(projects).findByPath("test");
			oneDev.when(() -> OneDev.getInstance(ProjectService.class)).thenReturn(projects);
			var resource = new ProjectSiteFileResource();
			assertThrows(UnauthorizedException.class,
					() -> resource.newResourceResponse(attributes(project, "secret/index.html")));
			for (var alias : List.of("SECRET", "Secret", "teamdocs", "PUBLIC")) {
				assertEquals(404, resource.newResourceResponse(attributes(project, alias + "/index.html")).getStatusCode());
				assertEquals(404, resource.newResourceResponse(attributes(project, alias + "/")).getStatusCode());
			}
			for (var site : List.of("public", "TeamDocs")) {
				assertEquals(12, resource.newResourceResponse(attributes(project, site + "/index.html")).getContentLength());
				assertEquals(12, resource.newResourceResponse(attributes(project, site + "/")).getContentLength());
			}
			assertEquals(3, ((DirectoryInfo) projects.getSiteArtifactInfo(1L, "")).getChildren().size());
		} finally {
			ThreadContext.unbindSubject();
			tester.destroy();
		}
	}

	@Test
	void checksNamedSitePermissionBeforeAccessingFilesAndDirectories() throws Exception {
		var tester = new WicketTester();
		var subject = mock(Subject.class);
		ThreadContext.bind(subject);
		try (var oneDev = mockStatic(OneDev.class)) {
			var projects = mock(ProjectService.class);
			var project = new Project();
			project.setId(1L);
			project.setName("test");
			project.setPath("test");
			when(projects.findByPath("test")).thenReturn(project);
			oneDev.when(() -> OneDev.getInstance(ProjectService.class)).thenReturn(projects);
			var resource = new ProjectSiteFileResource();
			var mapper = new ProjectResourceMapper("${project}/~sites/${site}", new SiteFileResourceReference());
			assertNull(mapper.mapRequest(new MockWebRequest(Url.parse("test/~sites"))));
			for (var projectPath : new String[] {"test", "parent/test"}) {
				project.setPath(projectPath);
				var params = ProjectSiteFileResource.paramsOf(project, "docs").set(0, "index.html");
				var url = mapper.mapHandler(new ResourceReferenceRequestHandler(new SiteFileResourceReference(), params));
				assertEquals(projectPath + "/~sites/docs/index.html", url.toString());
				var handler = (ResourceReferenceRequestHandler) mapper.mapRequest(new MockWebRequest(url));
				assertEquals(projectPath, handler.getPageParameters().get("project").toString());
				assertEquals("docs", handler.getPageParameters().get("site").toString());
				assertEquals("index.html", handler.getPageParameters().get(0).toString());
			}
			project.setPath("test");
			var handler = (ResourceReferenceRequestHandler) mapper.mapRequest(new MockWebRequest(
					Url.parse("test/~sites/docs/index.html?site=public&project=other")));
			assertEquals("test", handler.getPageParameters().get("project").toString());
			assertEquals("docs", handler.getPageParameters().get("site").toString());
			for (var path : new String[] {"docs", "docs/index.html", "docs/assets/logo.png"}) {
				assertThrows(UnauthorizedException.class, () -> resource.newResourceResponse(attributes(project, path)));
			}
			verify(projects, never()).getSiteArtifactInfo(anyLong(), anyString());

			when(projects.getSiteArtifactInfo(1L, "public/index.html"))
					.thenReturn(new FileInfo("public/index.html", 0, 42, "text/html"));
			assertEquals(42, resource.newResourceResponse(attributes(project, "public/index.html")).getContentLength());

			when(subject.isPermitted(any(Permission.class))).thenAnswer(it -> {
				var permission = (ProjectPermission) it.getArgument(0);
				return permission.getProject().equals(project)
						&& new AccessSites("docs").implies(permission.getPrivilege());
			});
			when(projects.getSiteArtifactInfo(1L, "docs"))
					.thenReturn(new DirectoryInfo("docs", 0, List.of(new FileInfo("docs/file.txt", 0, 1, "text/plain"))));
			var attrs = attributes(project, "docs/");
			var response = resource.newResourceResponse(attrs);
			response.getWriteCallback().writeData(attrs);
			var html = ((MockWebResponse) attrs.getResponse()).getTextResponse().toString();
			assertTrue(html.contains("href=\"file.txt\""));
			assertFalse(html.contains("href=\"../\""));
			assertThrows(RedirectToUrlException.class, () -> resource.newResourceResponse(attributes(project, "docs")));

			when(projects.getSiteArtifactInfo(1L, "docs/index.html"))
					.thenReturn(new FileInfo("docs/index.html", 0, 99, "text/html"));
			assertEquals(99, resource.newResourceResponse(attributes(project, "docs/")).getContentLength());
			assertEquals(404, resource.newResourceResponse(attributes(project, "docs/missing.txt")).getStatusCode());
			assertEquals(404, resource.newResourceResponse(attributes(project, "docs/" + SiteSyncUtils.FILE_VERSION)).getStatusCode());
			for (var path : new String[] {"public/../docs/secret", "public/..\\docs/secret"})
				assertThrows(ExplicitException.class, () -> resource.newResourceResponse(attributes(project, path)));

			// A new session without the site privilege must be denied.
			ThreadContext.bind(mock(Subject.class));
			assertThrows(UnauthorizedException.class, () -> resource.newResourceResponse(attributes(project, "docs/index.html")));
		} finally {
			ThreadContext.unbindSubject();
			tester.destroy();
		}
	}

	private Attributes attributes(Project project, String path) {
		var segments = path.split("/", -1);
		var params = ProjectSiteFileResource.paramsOf(project, segments[0]);
		for (int i = 1; i < segments.length; i++)
			params.set(i - 1, segments[i]);
		return new Attributes(new MockWebRequest(Url.parse("test/~sites/" + path)), new MockWebResponse(), params);
	}
}
