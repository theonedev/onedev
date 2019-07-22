package io.onedev.server.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Sets;

import io.onedev.server.model.Project;
import io.onedev.server.model.support.CommitMessageTransform;

public class IssueUtilsTest {

	@Test
	public void testParseFixedIssueNumbers() {
		Project project = new Project();
		assertEquals(Sets.newHashSet(11L, 22L, 33L), IssueUtils.parseFixedIssues(project, "fix #11, #22 and #33"));
		assertEquals(Sets.newHashSet(1L, 2L, 3L), IssueUtils.parseFixedIssues(project, "fix #1 #2 and #3"));
		assertEquals(Sets.newHashSet(11L, 22L, 33L), IssueUtils.parseFixedIssues(project, "resolves #11 , #22,#33"));
		assertEquals(Sets.newHashSet(1L, 2L, 3L, 5L, 6L), IssueUtils.parseFixedIssues(project, "resolves #1, #2,#3.#4 fixes #5 #6"));
		assertEquals(Sets.newHashSet(1L, 2L, 3L), IssueUtils.parseFixedIssues(project, "resolves #1, #2,#3.#4fixes #5 #6"));
		assertEquals(Sets.newHashSet(5L, 6L, 7L, 8L, 9L), IssueUtils.parseFixedIssues(project, "resolves fix #5 #6 and #7 and #8, #9 we finally #10"));
		
		CommitMessageTransform transform = new CommitMessageTransform();
		transform.setSearchFor("#(\\d+)");
		transform.setReplaceWith("<a href='http://track.example.com/issues/$1'>#$1</a>");
		project.getCommitMessageTransforms().add(transform);
		assertEquals(Sets.newHashSet(), IssueUtils.parseFixedIssues(project, "fix #11, #22 and #33"));
	}
	
}
