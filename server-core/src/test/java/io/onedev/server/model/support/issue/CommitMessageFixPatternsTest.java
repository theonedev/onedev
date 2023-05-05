package io.onedev.server.model.support.issue;

import org.junit.Test;

import static org.junit.Assert.*;

public class CommitMessageFixPatternsTest {

    @Test
    public void parseFixedIssues() {
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
				"fix #123,resolve  :  issue  #456  Closing path/to/test#100 Resolves issue path/to/test#200\n" +
				"feat(doc): this is a doc feat ( path/to/test#300 )");
		assertEquals(null, issues.get(0).getLeft());
		assertEquals(123L, issues.get(0).getRight().longValue());
		assertEquals(null, issues.get(1).getLeft());
		assertEquals(456L, issues.get(1).getRight().longValue());
		assertEquals("path/to/test", issues.get(2).getLeft());
		assertEquals(100L, issues.get(2).getRight().longValue());
		assertEquals("path/to/test", issues.get(3).getLeft());
		assertEquals(200L, issues.get(3).getRight().longValue());
		assertEquals("path/to/test", issues.get(4).getLeft());
		assertEquals(300L, issues.get(4).getRight().longValue());
    }
	
}