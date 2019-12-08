package io.onedev.server.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class UrlUtilsTest {

	@Test
	public void testDescribe() {
		assertEquals("File", UrlUtils.describe("file"));
		assertEquals("File", UrlUtils.describe("dir/file.txt"));
		assertEquals("File Name", UrlUtils.describe("dir/file-name.txt"));
		assertEquals("File Name", UrlUtils.describe("dir/file_name.txt"));
		assertEquals("File Name", UrlUtils.describe("dir.2/file_name.txt"));
		assertEquals("Google", UrlUtils.describe("www.google.com"));
		assertEquals("www.google.com", UrlUtils.describe("http://www.google.com"));
		assertEquals("www.google.com", UrlUtils.describe("https://www.google.com"));
	}
	
}
