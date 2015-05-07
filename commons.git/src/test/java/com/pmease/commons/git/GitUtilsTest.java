package com.pmease.commons.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.collect.Lists;

public class GitUtilsTest extends AbstractGitTest {

	@Test
	public void testReadLines() {
		String content = "中文测试中文测试中文测试中文测试\r\nsecond line";
		BlobText result = BlobText.from(content.getBytes()); 
		assertEquals(Lists.newArrayList("中文测试中文测试中文测试中文测试\r", "second line"), result.getLines());
		assertEquals(false, result.isHasEolAtEof());

		result = BlobText.from("\nhello\n\nworld\n".getBytes()); 
		assertEquals(Lists.newArrayList("", "hello", "", "world"), result.getLines());
	}
	
	@Test
	public void testComparePath() {
		assertTrue(GitPath.compare("dir1", "dir1/")==0);
		assertTrue(GitPath.compare("/dir1", "dir1/")==0);
		assertTrue(GitPath.compare("dir1", "dir2")<0);
		assertTrue(GitPath.compare("dir1", "dir1/dir2")<0);
		assertTrue(GitPath.compare("dir1/dir2/dir3/file", "dir1/dir3/file")<0);
		assertTrue(GitPath.compare("dir12", "dir1/dir2")>0);
	}
	
}
