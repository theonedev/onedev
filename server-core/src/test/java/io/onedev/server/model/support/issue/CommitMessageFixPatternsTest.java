package io.onedev.server.model.support.issue;

import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.AppLoaderMocker;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommitMessageFixPatternsTest extends AppLoaderMocker {

    @Test
    public void parseFixedIssues() {
		var projectManager = mock(ProjectManager.class);
		Mockito.when(AppLoader.getInstance(ProjectManager.class)).thenReturn(projectManager);
		var project = new Project();
		project.setId(1L);
		project.setKey("TESTPROJ");
		project.setPath("test/project");
		when(projectManager.load(1L)).thenReturn(project);
		when(projectManager.findByKey("TESTPROJ")).thenReturn(project);
		when(projectManager.findByPath("test/project")).thenReturn(project);
		
		var commitMessageFixPatterns = new CommitMessageFixPatterns();
		var entry = new CommitMessageFixPatterns.Entry();
		entry.setPrefix("(^|\\W)(fix|fixed|fixes|fixing|resolve|resolved|resolves|resolving|close|closed|closes|closing)[\\s:]+");
		entry.setSuffix("(?=$|\\W)");
		commitMessageFixPatterns.getEntries().add(entry);
		entry = new CommitMessageFixPatterns.Entry();
		entry.setPrefix("\\(\\s*");
		entry.setSuffix("\\s*\\)\\s*$");
		commitMessageFixPatterns.getEntries().add(entry);

		var issues = commitMessageFixPatterns.parseFixedIssues("" +
				"fix #123,resolve  :  issue  #456  Closing test/project#100 Resolves issue test/project#200\n" +
				"feat(doc): this is a doc feat ( test/project#300 )\n" +
				"feat: this is an improvement(UNKNOWN-400)\n" +
				"feat: this is an improvement(TESTPROJ-500)\n" +
				"fixes TESTPROJ-B600 and closes TESTPROJ-700\n" +
				"fix build #800 and fix issue #900", project);
		assertEquals(123L, issues.get(0).getNumber().longValue());
		assertEquals(456L, issues.get(1).getNumber().longValue());
		assertEquals(100L, issues.get(2).getNumber().longValue());
		assertEquals(200L, issues.get(3).getNumber().longValue());
		assertEquals(300L, issues.get(4).getNumber().longValue());
		assertEquals(500L, issues.get(5).getNumber().longValue());
		assertEquals(700L, issues.get(6).getNumber().longValue());
		assertEquals(900L, issues.get(7).getNumber().longValue());
    }

	@Override
	protected void setup() {
	}

	@Override
	protected void teardown() {
	}
}