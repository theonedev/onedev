package io.onedev.server.web.page.project.wiki;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

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
import io.onedev.server.git.BlobIdentFilter;
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
	public void wikiIgnoreFiltersPagesRelativeToWikiRoot() throws Exception {
		for (String folder : new String[] {null, "docs/wiki"}) {
			var project = mock(Project.class);
			var git = mock(GitService.class);
			var commit = ObjectId.fromString("1111111111111111111111111111111111111111");
			var page = mock(ProjectWikiPage.class, CALLS_REAL_METHODS);
			doReturn(project).when(page).getProject();
			set(page, "gitService", git);
			set(page, "folder", folder);
			set(page, "commitId", commit);
			String prefix = folder != null ? folder + "/" : "";
			when(git.getBlobIdent(eq(project), eq(commit), anyString())).thenAnswer(it ->
					new BlobIdent(commit.name(), it.getArgument(2), FileMode.TREE.getBits()));
			when(git.getChildren(project, commit, folder, BlobIdentFilter.ALL, false)).thenReturn(List.of(
					wikiEntry(commit, prefix + "Home.md", false),
					wikiEntry(commit, prefix + "AGENTS.md", false),
					wikiEntry(commit, prefix + "Root-only.md", false),
					wikiEntry(commit, prefix + "_Sidebar.md", false),
					wikiEntry(commit, prefix + "guide", true),
					wikiEntry(commit, prefix + "private", true)));
			when(git.getChildren(project, commit, prefix + "guide", BlobIdentFilter.ALL, false)).thenReturn(List.of(
					wikiEntry(commit, prefix + "guide/AGENTS.md", false),
					wikiEntry(commit, prefix + "guide/Root-only.md", false),
					wikiEntry(commit, prefix + "guide/Draft-one.md", false),
					wikiEntry(commit, prefix + "guide/Draft-keep.md", false)));
			when(git.getChildren(project, commit, prefix + "private", BlobIdentFilter.ALL, false)).thenReturn(List.of(
					wikiEntry(commit, prefix + "private/keep.md", false)));
			var ignoreIdent = wikiEntry(commit, prefix + ".wikiignore", false);
			byte[] rules = ("# Agent instructions\n\nAGENTS.md\n/Root-only.md\n"
					+ "**/Draft-*.md\n!guide/Draft-keep.md\nprivate/\n!private/keep.md\n")
					.getBytes(StandardCharsets.UTF_8);
			when(project.getBlob(ignoreIdent, false)).thenReturn(new Blob(ignoreIdent, commit, rules, rules.length));
			assertEquals(List.of("Home", "guide/Draft-keep", "guide/Root-only"), invoke(page, "getPages"));
			verify(git, never()).getChildren(project, commit, prefix + "private", BlobIdentFilter.ALL, false);

			// A revision without .wikiignore keeps the existing page listing behavior.
			when(project.getBlob(ignoreIdent, false)).thenReturn(null);
			assertEquals(List.of("AGENTS", "Home", "Root-only", "guide/AGENTS", "guide/Draft-keep",
					"guide/Draft-one", "guide/Root-only", "private/keep"), invoke(page, "getPages"));
		}
	}

	private static BlobIdent wikiEntry(ObjectId commit, String path, boolean directory) {
		return new BlobIdent(commit.name(), path, (directory ? FileMode.TREE : FileMode.REGULAR_FILE).getBits());
	}

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
				set(page, "markdownService", markdown);
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
				set(page, "gitService", git);
				set(page, "settingService", settings);
				set(page, "projectService", projects);
				set(page, "folder", "wiki");
				set(page, "commitId", ownerCommit);
				invoke(page, "resolveWikiProject");
				assertEquals(false, invoke(page, "canEdit", "Home.md"));
				invoke(page, "read", "Home");
				if (scenario.equals("allowed")) {
					assertNull(get(page, "wikiWarning"));
					verify(target).getBlob(argThat(it -> targetCommit.name().equals(it.revision)
							&& "Home.md".equals(it.path)), eq(false));
					invoke(page, "getPages");
					verify(target).getBlob(argThat(it -> targetCommit.name().equals(it.revision)
							&& ".wikiignore".equals(it.path)), eq(false));
				} else {
					assertNotNull(get(page, "wikiWarning"));
					verifyNoInteractions(target);
				}
			}
		}
	}

	private static void set(ProjectWikiPage page, String name, Object value) throws Exception {
		org.apache.commons.lang3.reflect.FieldUtils.writeField(page, name, value, true);
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
