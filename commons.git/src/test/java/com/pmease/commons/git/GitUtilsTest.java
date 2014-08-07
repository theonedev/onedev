package com.pmease.commons.git;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.pmease.commons.util.Charsets;

public class GitUtilsTest {

	@Test
	public void testReadLines() {
		String content = "中文测试\r\nsecond line";
		GitText result = GitText.from(content.getBytes(), Charsets.detectFrom(content.getBytes())); 
		assertEquals(Lists.newArrayList("中文测试\r", "second line"), result.getLines());
		assertEquals(false, result.isHasEolAtEof());

		result = GitText.from("\nhello\n\nworld\n".getBytes(), Charsets.UTF_8); 
		assertEquals(Lists.newArrayList("", "hello", "", "world"), result.getLines());
	}

}
