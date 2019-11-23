package io.onedev.server.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Sets;

public class IssueUtilsTest {

	@Test
	public void testParseFixedIssueNumbers() {
		assertEquals(Sets.newHashSet(1L, 2L, 3L, 4L), IssueUtils.parseFixedIssueNumbers(
				"fix issue #1,fixing issue #2 fix issue #3 and resolve issue #4: gogogo"));
	}
	
}
