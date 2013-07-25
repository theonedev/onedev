package com.pmease.commons.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void shouldMatchWildcard() {
		assertTrue(StringUtils.wildcardMatch("hello?world", "hello world"));
		assertFalse(StringUtils.wildcardMatch("hello?world", "hello  world"));
		assertTrue(StringUtils.wildcardMatch("hello*world", "hello  world"));
		assertTrue(StringUtils.wildcardMatch("**/*.java", "com/example/Test.java"));
		assertFalse(StringUtils.wildcardMatch("**/*.java", "com/example/test.c"));
		assertTrue(StringUtils.wildcardMatch("com/*.java", "com/example/Test.java"));
		assertTrue(StringUtils.wildcardMatch("com/**/*.java", "com/example/Test.java"));
		assertTrue(StringUtils.wildcardMatch("com/*.java", "com/Test.java"));
		assertTrue(StringUtils.wildcardMatch("src/**/*.java", "src/com/example/Test.java"));
		assertFalse(StringUtils.wildcardMatch("src/**/*.java", "resource/com/example/Test.java"));
	}

}
