package io.onedev.server.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Sets;

public class IssueUtilsTest {

	@Test
	public void testParseFixedIssueNumbers() {
		assertEquals(Sets.newHashSet(11L, 22L, 33L), IssueUtils.parseFixedIssues("fix #11, #22 and #33"));
		assertEquals(Sets.newHashSet(1L, 2L, 3L), IssueUtils.parseFixedIssues("fix #1 #2 and #3"));
		assertEquals(Sets.newHashSet(11L, 22L, 33L), IssueUtils.parseFixedIssues("resolves #11 , #22,#33"));
		assertEquals(Sets.newHashSet(1L, 2L, 3L, 5L, 6L), IssueUtils.parseFixedIssues("resolves #1, #2,#3.#4 fixes #5 #6"));
		assertEquals(Sets.newHashSet(1L, 2L, 3L), IssueUtils.parseFixedIssues("resolves #1, #2,#3.#4fixes #5 #6"));
		assertEquals(Sets.newHashSet(5L, 6L, 7L, 8L, 9L), IssueUtils.parseFixedIssues("resolves fix #5 #6 and #7 and #8, #9 we finally #10"));
	}
	
}
