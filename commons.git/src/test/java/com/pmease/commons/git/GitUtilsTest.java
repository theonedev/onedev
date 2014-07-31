package com.pmease.commons.git;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.pmease.commons.util.Charsets;

public class GitUtilsTest {

	@Test
	public void testReadLines() {
		assertEquals(null, GitUtils.readLines(new byte[]{1,2,3,4,5}));
		assertEquals(Charsets.ISO_8859_1, GitUtils.readLines("some text".getBytes()).getCharset());
		LineReadResult result = GitUtils.readLines("中文测试\r\nsecond line".getBytes()); 
		assertEquals(Lists.newArrayList("中文测试\r", "second line"), result.getLines());
		assertEquals(false, result.isHasEolAtEof());
		assertEquals(Charsets.UTF_8, result.getCharset());		

		result = GitUtils.readLines("\nhello\n\nworld\n".getBytes()); 
		assertEquals(Lists.newArrayList("", "hello", "", "world"), result.getLines());
	}

}
