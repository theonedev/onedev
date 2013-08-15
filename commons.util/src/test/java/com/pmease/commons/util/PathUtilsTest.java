package com.pmease.commons.util;

import static com.pmease.commons.util.PathUtils.matchLongest;
import static com.pmease.commons.util.PathUtils.parseRelative;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collection;

import org.junit.Test;

public class PathUtilsTest {

	@Test
	public void shouldParseRelative() {
		assertEquals(parseRelative("/path1/path2", "/path1"), "/path2");
		assertEquals(parseRelative("\\path1/path2", "/path1\\"), "/path2");
		assertEquals(parseRelative("path1\\path2/", "path1"), "/path2");
		assertEquals(parseRelative("/path1/path2", "/path1/path2"), "");
		assertNull(parseRelative("/path1/path2", "/path1/path3"));
		assertNull(parseRelative("/path1/path23", "/path1/path2"));
	}

	@Test
	public void shouldMatchLongest() {
		Collection<String> basePaths = EasyList.of("/path1/path2", "/path1/path2/path3");
		assertEquals(matchLongest(basePaths, "/path1/path2/path3/path4"), "/path1/path2/path3");
		assertNull(matchLongest(basePaths, "/path1/path23"));
		assertNull(matchLongest(basePaths, "/path1/path3"));
		
		basePaths = EasyList.of("/path1/path2", "/path1\\");
		assertEquals(matchLongest(basePaths, "path1\\path234"), "/path1\\");
		
		basePaths = EasyList.of("/asset1/asset2", "/asset1");
		assertEquals(matchLongest(basePaths, "/asset1/asset2/test.html"), "/asset1/asset2");
	}
	
}
