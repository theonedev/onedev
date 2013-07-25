package com.pmease.commons.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

public class PathUtilsTest {

	@Test
	public void shouldParseRelative() {
		assertEquals(PathUtils.parseRelative("/path1/path2", "/path1"), "/path2");
		assertEquals(PathUtils.parseRelative("\\path1/path2", "/path1\\"), "/path2");
		assertEquals(PathUtils.parseRelative("path1\\path2/", "path1"), "/path2");
		assertEquals(PathUtils.parseRelative("/path1/path2", "/path1/path2"), "");
		assertNull(PathUtils.parseRelative("/path1/path2", "/path1/path3"));
		assertNull(PathUtils.parseRelative("/path1/path23", "/path1/path2"));
	}

	@Test
	public void shouldMatchLongest() {
		Collection<String> basePaths = EasyList.of("/path1/path2", "/path1/path2/path3");
		assertEquals(PathUtils.matchLongest(basePaths, "/path1/path2/path3/path4"), "/path1/path2/path3");
		assertNull(PathUtils.matchLongest(basePaths, "/path1/path23"));
		assertNull(PathUtils.matchLongest(basePaths, "/path1/path3"));
		
		basePaths = EasyList.of("/path1/path2", "/path1\\");
		assertEquals(PathUtils.matchLongest(basePaths, "path1\\path234"), "/path1\\");
		
		basePaths = EasyList.of("/asset1/asset2", "/asset1");
		assertEquals(PathUtils.matchLongest(basePaths, "/asset1/asset2/test.html"), "/asset1/asset2");
	}
	
	@Test
	public void shouldMatchPath() {
		assertTrue(PathUtils.match("**//*.java", "com/example/Test.java"));
		assertFalse(PathUtils.match("**/*.java", "com/example/test.c"));
		assertFalse(PathUtils.match("com/*.java", "com/example/Test.java"));
		assertTrue(PathUtils.match("com/**//*.java", "com/example/Test.java"));
		assertTrue(PathUtils.match("com/*.java", "com/Test.java"));
		assertTrue(PathUtils.match("src///**/*.java", "src/com/example/Test.java"));
		assertFalse(PathUtils.match("/src/**/*.java", "src/com/example/Test.java"));
		assertFalse(PathUtils.match("src/**////*.java", "resource/com/example/Test.java"));
	}
	
}
