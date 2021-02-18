package io.onedev.server.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Sets;

import io.onedev.server.model.Project;

public class IssueUtilsTest {

	@Test
	public void testParseFixedIssueNumbers() {
		Project project = new Project();

		project.setName("test-23");
		assertEquals(Sets.newHashSet(6L), IssueUtils.parseFixedIssueNumbers(project,
				"Closing issue test#5, Fixing issue test-23#6"));
		
		project.setName("test");
		assertEquals(Sets.newHashSet(1L, 2L, 3L, 4L, 5L), IssueUtils.parseFixedIssueNumbers(project,
				"fix issue #1,fixing issue #2 fix issue #3 and resolve issue #4: gogogo and Closes issue test#5"));
	}
	
}
