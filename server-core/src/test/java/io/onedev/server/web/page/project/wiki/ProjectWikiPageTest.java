package io.onedev.server.web.page.project.wiki;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTester;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;
import org.jsoup.Jsoup;

import io.onedev.server.OneDev;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.service.GitService;
import io.onedev.server.markdown.MarkdownService;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.SystemSetting;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.resource.RawBlobResourceReference;

public class ProjectWikiPageTest {

	@Test
	public void sidebarLinksStayAtWikiRootWhenViewingNestedPages() throws Exception {
		var tester = new WicketTester();
		try {
			var project = mock(Project.class);
			when(project.getPath()).thenReturn("project");
			when(project.getMode(eq("main"), anyString())).thenReturn(FileMode.REGULAR_FILE.getBits());
			when(project.getBlob(any(BlobIdent.class), eq(false))).thenReturn(mock(Blob.class));
			var markdown = mock(MarkdownService.class);
			String source = "[Home](Home.md) [[Home]] ![Logo](images/logo.png)";
			when(markdown.render(source)).thenReturn(
					"<a href='Home.md'>Home</a><p>[[Home]]</p><img src='images/logo.png'>");
			when(markdown.process(anyString(), eq(project), isNull(), isNull(), eq(false)))
					.thenAnswer(it -> it.getArgument(0));
			var cycle = mock(RequestCycle.class);
			when(cycle.urlFor(eq(ProjectWikiPage.class), any(PageParameters.class))).thenAnswer(it -> {
				PageParameters params = it.getArgument(1);
				return "/project/~wiki/" + java.util.stream.IntStream.range(0, params.getIndexedCount())
						.mapToObj(i -> params.get(i).toString()).collect(java.util.stream.Collectors.joining("/"));
			});
			when(cycle.urlFor(any(RawBlobResourceReference.class), any(PageParameters.class)))
					.thenAnswer(it -> "/raw/" + ((PageParameters) it.getArgument(1)).get("file"));
			try (var oneDev = mockStatic(OneDev.class); var requestCycle = mockStatic(RequestCycle.class);
					var sprite = mockStatic(SpriteImage.class)) {
				oneDev.when(() -> OneDev.getInstance(MarkdownService.class)).thenReturn(markdown);
				requestCycle.when(RequestCycle::get).thenReturn(cycle);
				var page = mock(ProjectWikiPage.class, CALLS_REAL_METHODS);
				doReturn(project).when(page).getProject();
				set(page, "revision", "main");
				for (String folder : new String[] {"docs/wiki", null}) {
					set(page, "folder", folder);
					for (String currentPage : new String[] {"guides/Setup", "reference/api/Usage"}) {
						set(page, "page", currentPage);
						var sidebar = (MarkdownViewer) invoke(page, "sidebarViewer", source);
						var document = Jsoup.parseBodyFragment(render(sidebar, source));
						assertEquals(2, document.select("a").size());
						for (var link : document.select("a"))
							assertEquals("/project/~wiki/main/Home", link.attr("href"));
						String root = folder != null ? folder + "/" : "";
						assertEquals("/raw/" + root + "images/logo.png", document.selectFirst("img").attr("src"));

						var body = (MarkdownViewer) invoke(page, "viewer", "body", source);
						document = Jsoup.parseBodyFragment(render(body, source));
						String directory = currentPage.substring(0, currentPage.lastIndexOf('/') + 1);
						for (var link : document.select("a"))
							assertEquals("/project/~wiki/main/" + directory + "Home", link.attr("href"));
						assertEquals("/raw/" + root + directory + "images/logo.png", document.selectFirst("img").attr("src"));
					}
				}
			}
		} finally {
			tester.destroy();
		}
	}

	private static String render(MarkdownViewer viewer, String markdown) throws Exception {
		var method = viewer.getClass().getDeclaredMethod("renderMarkdown", String.class);
		method.setAccessible(true);
		return (String) method.invoke(viewer, markdown);
	}

	@Test
	public void resolvesSubmoduleBeforeReadingHomeAndAlwaysDeniesEdits() throws Exception {
		var owner = mock(Project.class);
		var target = mock(Project.class);
		var git = mock(GitService.class);
		var projects = mock(ProjectService.class);
		var settings = mock(SettingService.class);
		var system = mock(SystemSetting.class);
		when(settings.getSystemSetting()).thenReturn(system);
		when(system.getServerUrl()).thenReturn("https://server");
		when(owner.getPath()).thenReturn("app");
		when(target.getId()).thenReturn(2L);
		when(projects.load(2L)).thenReturn(target);
		var ownerCommit = ObjectId.fromString("1111111111111111111111111111111111111111");
		var targetCommit = ObjectId.fromString("2222222222222222222222222222222222222222");
		var ident = new BlobIdent(ownerCommit.name(), "wiki", FileMode.GITLINK.getBits());
		when(git.getBlobIdent(owner, ownerCommit, "wiki")).thenReturn(ident);
		when(target.getObjectId(targetCommit.name(), false)).thenReturn(targetCommit);
		when(target.getRevCommit(targetCommit, false)).thenReturn(mock(RevCommit.class));
		try (var oneDev = mockStatic(OneDev.class); var security = mockStatic(SecurityUtils.class)) {
			oneDev.when(() -> OneDev.getInstance(GitService.class)).thenReturn(git);
			oneDev.when(() -> OneDev.getInstance(ProjectService.class)).thenReturn(projects);
			oneDev.when(() -> OneDev.getInstance(SettingService.class)).thenReturn(settings);
			for (String scenario : new String[] {"external", "missing", "denied", "allowed"}) {
				clearInvocations(target);
				String url = scenario.equals("external") ? "https://other/docs" : "https://server/docs";
				byte[] content = (url + ":" + targetCommit.name()).getBytes(StandardCharsets.UTF_8);
				when(owner.getBlob(ident, true)).thenReturn(new Blob(ident, ownerCommit, content, content.length));
				when(projects.findByPath("docs")).thenReturn(scenario.equals("missing") ? null : target);
				security.when(() -> SecurityUtils.canReadCode(target)).thenReturn(scenario.equals("allowed"));
				var page = mock(ProjectWikiPage.class, CALLS_REAL_METHODS);
				doReturn(owner).when(page).getProject();
				set(page, "folder", "wiki");
				set(page, "commitId", ownerCommit);
				invoke(page, "resolveWikiProject");
				assertEquals(false, invoke(page, "canEdit", "Home.md"));
				invoke(page, "read", "Home");
				if (scenario.equals("allowed")) {
					assertNull(get(page, "wikiWarning"));
					verify(target).getBlob(argThat(it -> targetCommit.name().equals(it.revision)
							&& "Home.md".equals(it.path)), eq(false));
				} else {
					assertNotNull(get(page, "wikiWarning"));
					verifyNoInteractions(target);
				}
			}
		}
	}

	private static void set(ProjectWikiPage page, String name, Object value) throws Exception {
		var field = ProjectWikiPage.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(page, value);
	}

	private static Object get(ProjectWikiPage page, String name) throws Exception {
		var field = ProjectWikiPage.class.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(page);
	}

	private static Object invoke(ProjectWikiPage page, String name, String... args) throws Exception {
		var method = ProjectWikiPage.class.getDeclaredMethod(name,
				java.util.Collections.nCopies(args.length, String.class).toArray(Class<?>[]::new));
		method.setAccessible(true);
		return method.invoke(page, (Object[]) args);
	}
}
