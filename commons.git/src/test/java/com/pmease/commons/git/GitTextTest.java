package com.pmease.commons.git;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Lists;

public class GitTextTest {

	@Test
	public void test() {
		GitText result = new GitText(Lists.newArrayList(" hello \tworld \t \r"), true);
		assertEquals(" hello \tworld \t ", result.ignoreEOL().getLines().get(0));
		assertEquals(" hello \tworld", result.ignoreEOLWhitespaces().getLines().get(0));
		assertEquals("hello world", result.ignoreWhitespaces().getLines().get(0));
	}

}
