package io.onedev.server.entityreference;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.AppLoaderMocker;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.HtmlUtils;
import io.onedev.server.util.ProjectScopedNumber;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReferenceParserTest extends AppLoaderMocker {

    @Test
    public void parseReferences() {
		var projectManager = mock(ProjectManager.class);
		Mockito.when(AppLoader.getInstance(ProjectManager.class)).thenReturn(projectManager);		
		var project1 = new Project();
		project1.setId(1L);
		project1.setPath("test/project1");
		when(projectManager.load(1L)).thenReturn(project1);
		when(projectManager.findByPath("test/project1")).thenReturn(project1);
		var project2 = new Project();
		project2.setId(2L);
		project2.setPath("test/project2");
		when(projectManager.load(2L)).thenReturn(project2);
		when(projectManager.findByPath("test/project2")).thenReturn(project2);

		var parser = new ReferenceParser(Issue.class);

		assertTrue(parser.parseReferences("pull request #1", project1).isEmpty());
		assertTrue(parser.parseReferences("pr #1", project1).isEmpty());
		assertTrue(parser.parseReferences("build #1", project1).isEmpty());
		assertTrue(parser.parseReferences("pull request test/project1#1", project1).isEmpty());
		assertTrue(parser.parseReferences("pr test/project1#1", project1).isEmpty());
		assertTrue(parser.parseReferences("build test/project1#1", project1).isEmpty());

		var issueNumbers = parser.parseReferences("pull request #1 #2", project1);
		assertEquals(1, issueNumbers.size());
		assertEquals(2L, issueNumbers.get(0).getNumber().longValue());
		
		issueNumbers = parser.parseReferences("#1", project1);
		assertEquals(1L, issueNumbers.get(0).getNumber().longValue());

		issueNumbers = parser.parseReferences("#1 and issue #2", project1);
		assertEquals(1L, issueNumbers.get(0).getNumber().longValue());
		assertEquals(2L, issueNumbers.get(1).getNumber().longValue());

		issueNumbers = parser.parseReferences("feat (docs): this is doc (#3)", project1);
		assertEquals(3L, issueNumbers.get(0).getNumber().longValue());
		
		issueNumbers = parser.parseReferences("test/project2#1 issue test/project2#2", project1);
		assertEquals("test/project2", issueNumbers.get(0).getProject().getPath());
		assertEquals(1L, issueNumbers.get(0).getNumber().longValue());
		assertEquals("test/project2", issueNumbers.get(1).getProject().getPath());
		assertEquals(2L, issueNumbers.get(1).getNumber().longValue());

		parser = new ReferenceParser(PullRequest.class);

		var pullRequestNumbers = parser.parseReferences("pull request #1 and pr #2", project1);
		assertEquals(1L, pullRequestNumbers.get(0).getNumber().longValue());
		assertEquals(2L, pullRequestNumbers.get(1).getNumber().longValue());

		parser = new ReferenceParser(Build.class);

		var buildNumbers = parser.parseReferences("build #1", project1);
		assertEquals(1L, buildNumbers.get(0).getNumber().longValue());

		var doc = HtmlUtils.parse("issue #1 and #2");
		parser = new ReferenceParser(Issue.class) {
			@Override
			protected String toHtml(ProjectScopedNumber referenceable, String referenceText) {
				return "<a href='" + referenceable + "'>" + referenceText + "</a>";
			}
		};
		issueNumbers = parser.parseReferences(doc, project1);
		assertEquals(1L, issueNumbers.get(0).getNumber().longValue());
		assertEquals(2L, issueNumbers.get(1).getNumber().longValue());
		assertEquals("<a href=\"test/project1#1\">issue #1</a> and <a href=\"test/project1#2\">#2</a>", doc.body().html());
    }

	@Override
	protected void setup() {
		
	}

	@Override
	protected void teardown() {

	}
}