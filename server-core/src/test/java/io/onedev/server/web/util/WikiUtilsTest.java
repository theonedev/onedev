package io.onedev.server.web.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jsoup.Jsoup;
import org.junit.Test;

import io.onedev.server.model.Project;
import io.onedev.server.model.Role;
import io.onedev.server.model.support.WikiSetting;
import io.onedev.server.model.support.role.CodePrivilege;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.WriteCode;

public class WikiUtilsTest {

	@Test
	public void insertsWikiReferencesRelativeToCurrentPage() {
		assertEquals("[[../Home|Home]]", WikiUtils.pageReference("wiki", "wiki/Guides/Page.md", "wiki/Home.md", null));
		assertEquals("[[../Home|Home]]", WikiUtils.pageReference("wiki", "wiki/Guides/Page.md", "wiki/Home.md", ""));
		assertEquals("[[Setup]]", WikiUtils.pageReference("wiki", "wiki/Guides/Page.md", "wiki/Guides/Setup.md", null));
		assertEquals("[[../Home|Home]]", WikiUtils.pageReference("wiki", "wiki/Guides/Page.md", "wiki/Home.md", "Home"));
	}

	@Test
	public void insertsReadableNamesInWikiReferences() {
		assertEquals("[[Installation Guide]]", WikiUtils.pageReference(
				"wiki", "wiki/Home.md", "wiki/Installation-Guide.md", null));
		assertEquals("[[folder/Installation Guide|Installation Guide]]", WikiUtils.pageReference(
				"wiki", "wiki/Home.md", "wiki/folder/Installation-Guide.md", null));
		assertEquals("[[../Installation Guide|Installation Guide]]", WikiUtils.pageReference(
				"wiki", "wiki/folder/Home.md", "wiki/Installation-Guide.md", ""));
		assertEquals("[[folder/Installation Guide|Read-this guide]]", WikiUtils.pageReference(
				"wiki", "wiki/Home.md", "wiki/folder/Installation-Guide.md", "Read-this guide"));
	}

	@Test
	public void resolvesRelativeWikiReferencesWithinRepository() {
		String html = WikiUtils.resolvePageLinks(
				"<p>[[../Sibling#intro|Up]] [[../../README]] [[./Child]] [[Home]] [[../../../outside]]</p>",
				destination -> WikiUtils.resolvePagePath("wiki/guide/Page.md", destination),
				path -> "/files/" + path, path -> path.equals("wiki/Sibling.md"),
				path -> "/add/" + path, "/icons#plus");
		var document = Jsoup.parseBodyFragment(html);
		assertEquals("/files/wiki/Sibling.md#intro", document.selectFirst("a").attr("href"));
		assertEquals("/add/wiki/Sibling.md", document.selectFirst("a.add-missing").attr("href"));
		assertEquals(1, document.select("a[href=/files/README.md]").size());
		assertEquals(1, document.select("a[href=/files/wiki/guide/Child.md]").size());
		assertEquals(1, document.select("a[href=/files/wiki/guide/Home.md]").size());
		assertTrue(document.text().contains("[[../../../outside]]"));
	}

	@Test
	public void resolvesPageNamesAnchorsAndMissingCreationLinks() {
		String html = WikiUtils.resolvePageLinks(
				"<p>[[Missing page#heading|Title]] [[#local]] [[../invalid]] [[Existing]]</p>",
				page -> "/wiki/" + page, page -> page.equals("Missing-page"),
				page -> "/new/" + page, "/icons.svg#plus");
		var document = org.jsoup.Jsoup.parse(html);
		assertEquals("/wiki/Missing-page#heading", document.selectFirst("a").attr("href"));
		assertEquals("/new/Missing-page", document.selectFirst("a.add-missing").attr("href"));
		assertEquals(1, document.select("span.missing").size());
		assertEquals(1, document.select("a[href=#local]").size());
		assertEquals(1, document.select("a[href=/wiki/Existing]").size());
		assertTrue(document.text().contains("[[../invalid]]"));
	}

	@Test
	public void selectedPagesUseWikiReferences() {
		assertEquals("[[Home]]", WikiUtils.pageReference("wiki", "wiki/Home.md", "wiki/Home.md", null));
		assertEquals("[[Guides/Setup|Setup]]", WikiUtils.pageReference("docs/wiki", "docs/wiki/Home.md", "docs/wiki/Guides/Setup.md", null));
		assertEquals("[[Guides/Setup|Read this]]", WikiUtils.pageReference("wiki", "wiki/Home.md", "wiki/Guides/Setup.md", "Read this"));
		assertThrows(IllegalArgumentException.class, () -> WikiUtils.pageReference("wiki", "wiki/Home.md", "other/Home.md", null));
		assertThrows(IllegalArgumentException.class, () -> WikiUtils.pageReference("wiki", "wiki/Home.md", "wiki/image.png", null));
	}

	@Test
	public void marksMissingPagesWithoutTouchingCodeOrExistingLinks() {
		String html = WikiUtils.linkPages("<p>[[Home]] [[Missing#heading|Title]]</p><code>[[Missing]]</code><a href='/existing'>[[Missing]]</a>",
				page -> "/wiki/" + page, page -> page.startsWith("Missing"),
				page -> "/wiki/add/" + page, "/icons.svg#plus");
		var document = org.jsoup.Jsoup.parse(html);
		assertEquals(1, document.select("span.missing").size());
		assertEquals("!!missing!!", document.selectFirst("span.missing").text());
		assertEquals("Title", document.selectFirst("span.missing").previousElementSibling().text());
		assertEquals("/wiki/Missing#heading", document.selectFirst("span.missing").previousElementSibling().attr("href"));
		assertEquals("/wiki/add/Missing#heading", document.selectFirst("a.add-missing").attr("href"));
		assertEquals("Add this page", document.selectFirst("a.add-missing").attr("title"));
		assertEquals(1, document.select("a.add-missing svg.icon use").size());
		assertEquals("[[Missing]]", document.selectFirst("code").text());
		assertEquals("[[Missing]]", document.selectFirst("a[href=/existing]").text());
	}

	@Test
	public void confinesPagesToWikiFolder() {
		assertEquals("docs/wiki/Getting-started.md", WikiUtils.pagePath("docs/wiki", "Getting-started"));
		for (String invalid : new String[] {"../secret", "/secret", "a/../../secret"}) {
			assertThrows(invalid, io.onedev.server.exception.NotAcceptableException.class,
					() -> WikiUtils.pagePath("wiki", invalid));
		}
	}

	@Test
	public void inheritsNearestConfiguredFolder() {
		Project parent = new Project();
		Project child = new Project();
		child.setParent(parent);
		assertEquals("wiki", child.getWikiFolder());
		parent.getWikiSetting().setFolder("documentation");
		assertEquals("documentation", child.getWikiFolder());
		child.getWikiSetting().setFolder("docs/wiki");
		assertEquals("docs/wiki", child.getWikiFolder());
		child.getWikiSetting().setFolder(null);
		assertEquals("documentation", child.getWikiFolder());
	}

	@Test
	public void projectAccessDoesNotGrantCodeRead() {
		assertTrue(new ReadCode().implies(new AccessProject()));
		assertTrue(new WriteCode().implies(new AccessProject()));
		assertFalse(new AccessProject().implies(new ReadCode()));
		assertFalse(new AccessProject().implies(new WriteCode()));
		Role role = new Role();
		role.setEditableIssueFields(new io.onedev.server.model.support.role.NoneIssueFields());
		assertTrue(role.implies(new AccessProject()));
		assertFalse(role.implies(new ReadCode()));
		role.setCodePrivilege(CodePrivilege.READ);
		assertTrue(role.implies(new AccessProject()));
	}

	@Test
	public void projectAccessReadsOnlyEnabledWikiFiles() {
		var project = mock(Project.class);
		when(project.isWikiManagement()).thenReturn(true);
		when(project.getWikiFolder()).thenReturn("docs/wiki");
		var subject = mock(org.apache.shiro.subject.Subject.class);
		when(subject.isPermitted(any(ProjectPermission.class))).thenAnswer(invocation ->
				((ProjectPermission) invocation.getArgument(0)).getPrivilege() instanceof AccessProject);

		assertTrue(SecurityUtils.canReadFile(subject, project, "docs/wiki/Home.md"));
		assertTrue(SecurityUtils.canReadFile(subject, project, "docs/wiki/image.png"));
		assertFalse(SecurityUtils.canReadFile(subject, project, "private.txt"));
		assertFalse(SecurityUtils.canReadFile(subject, project, "docs/wiki-other/Home.md"));
		assertFalse(SecurityUtils.canReadFile(subject, project, null));
		when(project.isWikiManagement()).thenReturn(false);
		assertFalse(SecurityUtils.canReadFile(subject, project, "docs/wiki/Home.md"));
		when(project.isWikiManagement()).thenReturn(true);
		when(subject.isPermitted(any(ProjectPermission.class))).thenReturn(false);
		assertFalse(SecurityUtils.canReadFile(subject, project, "docs/wiki/Home.md"));
		when(subject.isPermitted(any(ProjectPermission.class))).thenAnswer(invocation ->
				((ProjectPermission) invocation.getArgument(0)).getPrivilege() instanceof ReadCode);
		assertTrue(SecurityUtils.canReadFile(subject, project, "private.txt"));
	}

	@Test
	public void linksProseButPreservesCodeAndExistingLinks() {
		String html = WikiUtils.linkPages("<p>[[Page]] and [[Other|Label]]</p><pre>[[Code]]</pre><code>[[Inline]]</code><a href='/x'>[[Link]]</a>", s -> "/wiki/" + s);
		assertTrue(html.contains("<a href=\"/wiki/Page\">Page</a>"));
		assertTrue(html.contains("<a href=\"/wiki/Other\">Label</a>"));
		assertTrue(html.contains("<pre>[[Code]]</pre>"));
		assertTrue(html.contains("<code>[[Inline]]</code>"));
		assertTrue(html.contains("<a href=\"/x\">[[Link]]</a>"));
		assertTrue(WikiUtils.linkPages("<p>[[Page|&lt;script&gt;]]</p>", s -> "/wiki/" + s).contains("&lt;script&gt;"));
	}

	@Test
	public void resolvesOrdinaryRelativeLinksFromWikiPagePath() {
		String html = WikiUtils.resolveRelativeLinks(
				"<p><a href='../Missing.md#linux'>Missing</a><img src='./images/missing.png?size=2'></p>",
				"docs/wiki/Guides/Home.md", path -> "/link/" + path, path -> "/raw/" + path,
				path -> true, path -> "/add/" + path, "/icons.svg#plus");
		var document = org.jsoup.Jsoup.parse(html);
		assertEquals("/link/docs/wiki/Missing.md#linux", document.selectFirst("a").attr("href"));
		assertEquals("/raw/docs/wiki/Guides/images/missing.png?size=2", document.selectFirst("img").attr("src"));
		assertEquals(2, document.select("span.missing").size());
		assertEquals("/add/docs/wiki/Missing.md", document.selectFirst("a.add-missing").attr("href"));
		assertEquals("Add this file", document.selectFirst("a.add-missing").attr("title"));
		assertEquals(1, document.select("a.add-missing svg.icon use").size());
	}

	@Test
	public void identifiesFilesContainedByWikiFolder() {
		assertTrue(WikiUtils.isUnderFolder("docs/wiki", "docs/wiki/image.png"));
		assertFalse(WikiUtils.isUnderFolder("docs/wiki", "docs/wiki-other/image.png"));
		assertFalse(WikiUtils.isUnderFolder("docs/wiki", "docs/image.png"));
	}

	@Test
	public void validatesFolderAsRelativePath() {
		try (var factory = javax.validation.Validation.buildDefaultValidatorFactory()) {
			var setting = new WikiSetting();
			setting.setFolder("../outside");
			var violations = factory.getValidator().validate(setting);
			assertEquals(1, violations.size());
			assertEquals("folder", violations.iterator().next().getPropertyPath().toString());
			setting.setFolder("/absolute");
			assertEquals(1, factory.getValidator().validate(setting).size());
			setting.setFolder("docs/wiki");
			assertTrue(factory.getValidator().validate(setting).isEmpty());
			setting.setFolder(null);
			assertTrue(factory.getValidator().validate(setting).isEmpty());
		}
	}

}
