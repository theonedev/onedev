package com.pmease.commons.git;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.pmease.commons.util.Charsets;

public class LineReadResultTest {

	@Test
	public void test() {
		LineReadResult result = new LineReadResult(Lists.newArrayList(" hello \tworld \t \r"), Charsets.UTF_8, true);
		assertEquals(" hello \tworld \t ", result.ignoreEOL().getLines().get(0));
		assertEquals(" hello \tworld", result.ignoreEOLWhitespaces().getLines().get(0));
		assertEquals("hello world", result.ignoreWhitespaces().getLines().get(0));
	}

}
