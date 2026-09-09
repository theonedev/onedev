package io.onedev.server.web.util;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.jsoup.Jsoup;
import org.junit.Test;

import io.onedev.server.model.Project;
import io.onedev.server.model.support.wiki.SpecifiedPath;
import io.onedev.server.model.support.wiki.RepositoryRoot;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.wiki.ProjectWikiPage;
import io.onedev.server.web.resource.RawBlobResourceReference;

public class WikiLinkResolverTest {

	@Test
	public void wikiReferencesResolveInViewsAndEditorPreviews() {
		Project project = mock(Project.class);
		when(project.getPath()).thenReturn("project");
		when(project.getWikiFolder()).thenReturn(specifiedPath("docs/wiki"));
		BlobRenderContext context = mock(BlobRenderContext.class);
		when(context.getProject()).thenReturn(project);
		when(context.getBlobIdent()).thenReturn(new BlobIdent("main", "docs/wiki/Home.md"));
		when(context.getNewPath()).thenReturn("docs/wiki/Guides/Setup.md");
		RequestCycle cycle = mock(RequestCycle.class);
		when(cycle.urlFor(eq(ProjectWikiPage.class), any(PageParameters.class))).thenAnswer(invocation -> {
			PageParameters params = invocation.getArgument(1);
			return "/project/~wiki/" + java.util.stream.IntStream.range(0, params.getIndexedCount())
					.mapToObj(i -> params.get(i).toString()).collect(java.util.stream.Collectors.joining("/"));
		});
		try (var requestCycle = mockStatic(RequestCycle.class); var sprite = mockStatic(SpriteImage.class)) {
			requestCycle.when(RequestCycle::get).thenReturn(cycle);
			for (Mode mode : new Mode[] {Mode.VIEW, Mode.EDIT, Mode.ADD}) {
				when(context.getMode()).thenReturn(mode);
				String html = WikiLinkResolver.resolveWikiLinks(
						"<p>[[Getting started#install|Start here]]</p><code>[[Untouched]]</code>", context);
				var document = Jsoup.parseBodyFragment(html);
				assertEquals("/project/~wiki/main/" + (mode == Mode.VIEW ? "" : "Guides/")
						+ "Getting-started#install", document.selectFirst("p a").attr("href"));
				assertEquals("Start here", document.selectFirst("p a").text());
				assertEquals("[[Untouched]]", document.selectFirst("code").text());
			}
		}
	}

	@Test
	public void previewUsesEditedPathToDetermineWhetherFileIsWiki() {
		Project project = mock(Project.class);
		when(project.getWikiFolder()).thenReturn(specifiedPath("docs/wiki"));
		BlobRenderContext context = mock(BlobRenderContext.class);
		when(context.getProject()).thenReturn(project);
		when(context.getBlobIdent()).thenReturn(new BlobIdent("main", "docs/wiki/Home.md"));
		when(context.getMode()).thenReturn(Mode.EDIT);
		when(context.getNewPath()).thenReturn("README.md");
		String html = "<p>[[Getting started]]</p>";
		assertEquals(html, WikiLinkResolver.resolveWikiLinks(html, context));
		when(context.getMode()).thenReturn(Mode.ADD);
		when(context.getNewPath()).thenReturn(null);
		assertEquals(html, WikiLinkResolver.resolveWikiLinks(html, context));
		assertEquals(html, WikiLinkResolver.resolveWikiLinks(html, null));
	}

	@Test
	public void routesOrdinaryLinksImagesAndWikiReferencesConsistently() {
		Project project = mock(Project.class);
		when(project.getPath()).thenReturn("project");
		when(project.getWikiFolder()).thenReturn(specifiedPath("wiki"));
		when(project.getMode(eq("main"), anyString())).thenReturn(FileMode.REGULAR_FILE.getBits());
		RequestCycle cycle = mock(RequestCycle.class);
		UrlRenderer renderer = mock(UrlRenderer.class);
		when(cycle.getUrlRenderer()).thenReturn(renderer);
		when(renderer.renderFullUrl(any(Url.class))).thenAnswer(it -> "http://localhost" + it.getArgument(0));
		when(cycle.urlFor(eq(ProjectWikiPage.class), any(PageParameters.class)))
				.thenAnswer(it -> url("~wiki", it.getArgument(1)));
		when(cycle.urlFor(eq(ProjectBlobPage.class), any(PageParameters.class)))
				.thenAnswer(it -> url("~files", it.getArgument(1)));
		when(cycle.urlFor(any(RawBlobResourceReference.class), any(PageParameters.class)))
				.thenAnswer(it -> {
					PageParameters params = it.getArgument(1);
					assertEquals(0, params.getIndexedCount());
					return "/project/~raw?revision=" + params.get("revision") + "&file=" + params.get("file");
				});
		try (var requestCycle = mockStatic(RequestCycle.class);
				var sprite = mockStatic(SpriteImage.class); var security = mockStatic(SecurityUtils.class)) {
			requestCycle.when(RequestCycle::get).thenReturn(cycle);
			var resolver = new WikiLinkResolver(project, "main", "wiki", "wiki/guide/Page.md", "guide/Page");
			String html = "<p>[[Sibling|Read this]] [[../../README|Readme]] [[Missing]]</p>"
					+ "<a href='Sibling.md#intro'>ordinary</a><a href='../manual.pdf'>manual</a>"
					+ "<img src='../logo.png'><img src='../../logo.png?v=2#preview'>"
					+ "<a href='../../manual.pdf#page=3'>outside</a>";
			var document = Jsoup.parseBodyFragment(resolver.resolve(html));
			assertEquals("/project/~wiki/main/guide/Sibling", document.selectFirst("p a").attr("href"));
			assertEquals("Read this", document.selectFirst("p a").text());
			assertEquals(1, document.select("a[href=/project/~files/main/README.md]").size());
			assertEquals(1, document.select("a[href=/project/~wiki/main/guide/Sibling#intro]").size());
			assertEquals(1, document.select("a[href='/project/~raw?revision=main&file=wiki/manual.pdf']").size());
			assertEquals("/project/~raw?revision=main&file=wiki/logo.png", document.select("img").get(0).attr("src"));
			assertEquals("/project/~files/main/logo.png?raw=true&v=2#preview", document.select("img").get(1).attr("src"));
			assertEquals(1, document.select("a[href=/project/~files/main/manual.pdf#page=3]").size());
			assertEquals(3, document.select("span.missing").size());
			assertTrue(document.select("a.add-missing").isEmpty());
			var preview = Jsoup.parseBodyFragment(resolver.resolvePageLinks(html));
			assertEquals(document.selectFirst("p a").attr("href"), preview.selectFirst("p a").attr("href"));
			assertEquals("../logo.png", preview.selectFirst("img").attr("src"));
		}
	}

	@Test
	public void routesRepositoryRootPagesAndCreationLinks() {
		var project = mock(Project.class);
		when(project.getPath()).thenReturn("project");
		when(project.getWikiFolder()).thenReturn(new RepositoryRoot());
		var context = mock(BlobRenderContext.class);
		when(context.getProject()).thenReturn(project);
		when(context.getMode()).thenReturn(Mode.VIEW);
		when(context.getBlobIdent()).thenReturn(new BlobIdent("main", "Home.md"));
		var cycle = mock(RequestCycle.class);
		when(cycle.urlFor(eq(ProjectWikiPage.class), any(PageParameters.class))).thenAnswer(it -> {
			PageParameters params = it.getArgument(1);
			if (params.get("new").toBoolean(false))
				assertEquals("guide/Getting started-guide", params.get("initial-name").toString());
			return url("~wiki", params) + (params.get("new").toBoolean(false) ? "?new=true" : "");
		});
		when(cycle.urlFor(any(RawBlobResourceReference.class), any(PageParameters.class)))
				.thenAnswer(it -> "/raw/" + ((PageParameters) it.getArgument(1)).get("file"));
		try (var requestCycle = mockStatic(RequestCycle.class);
				var sprite = mockStatic(SpriteImage.class); var security = mockStatic(SecurityUtils.class)) {
			requestCycle.when(RequestCycle::get).thenReturn(cycle);
			sprite.when(() -> SpriteImage.getVersionedHref(io.onedev.server.web.asset.icon.IconScope.class, "plus")).thenReturn("/icons.svg#plus");
			security.when(() -> SecurityUtils.canEditWikiPage(project, "main", "guide/Getting-started-guide.md")).thenReturn(true);
			var document = Jsoup.parseBodyFragment(WikiLinkResolver.resolveWikiLinks("[[guide/Getting started-guide#intro|Start]]", context));
			assertEquals("/project/~wiki/main/guide/Getting-started-guide#intro", document.selectFirst("a").attr("href"));
			assertEquals("/project/~wiki/main/guide/Getting-started-guide?new=true", document.selectFirst("a.add-missing").attr("href"));
			var resolver = new WikiLinkResolver(project, "main", null, "guide/Setup.md", "guide/Setup");
			document = Jsoup.parseBodyFragment(resolver.resolve("<a href='../Home.md'>Home</a><img src='../logo.png'>"));
			assertEquals("/project/~wiki/main/Home", document.selectFirst("a").attr("href"));
			assertEquals("/raw/logo.png", document.selectFirst("img").attr("src"));
		}
	}

	private static String url(String route, PageParameters params) {
		String path = java.util.stream.IntStream.range(0, params.getIndexedCount())
				.mapToObj(i -> params.get(i).toString()).collect(java.util.stream.Collectors.joining("/"));
		return "/project/" + route + "/" + path + (params.get("raw").toBoolean(false) ? "?raw=true" : "");
	}
	private static SpecifiedPath specifiedPath(String path) {
		var folder = new SpecifiedPath();
		folder.setPath(path);
		return folder;
	}

}
