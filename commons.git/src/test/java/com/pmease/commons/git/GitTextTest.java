package com.pmease.commons.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class GitTextTest {

	@Test
	public void test() {
		BlobText result = BlobText.from(" hello \tworld \t \r".getBytes());
		assertNotNull(result);
		assertEquals(" hello \tworld \t ", result.ignoreEOL().getLines().get(0));
		assertEquals(" hello \tworld", result.ignoreEOLSpaces().getLines().get(0));
		assertEquals("hello world", result.ignoreChangeSpaces().getLines().get(0));
	}

}
