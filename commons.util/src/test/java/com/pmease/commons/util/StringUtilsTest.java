package com.pmease.commons.util;

import static org.junit.Assert.*;
import static com.pmease.commons.util.StringUtils.wildcardMatch;

import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void shouldMatchWildcard() {
		assertTrue(wildcardMatch("hello?world", "hello world"));
		assertFalse(wildcardMatch("hello?world", "hello  world"));
		assertTrue(wildcardMatch("hello*world", "hello  world"));
		assertTrue(wildcardMatch("**/*.java", "com/example/Test.java"));
		assertFalse(wildcardMatch("**/*.java", "com/example/test.c"));
		assertTrue(wildcardMatch("com/*.java", "com/example/Test.java"));
		assertTrue(wildcardMatch("com/**/*.java", "com/example/Test.java"));
		assertTrue(wildcardMatch("com/*.java", "com/Test.java"));
		assertTrue(wildcardMatch("src/**/*.java", "src/com/example/Test.java"));
		assertFalse(wildcardMatch("src/**/*.java", "resource/com/example/Test.java"));
		
		assertTrue(wildcardMatch("-*.java, *", "hello.c"));
		assertFalse(wildcardMatch("-*.java, *", "hello.java"));
		assertFalse(wildcardMatch("-*.java, *.java, *.c", "hello.txt"));
		assertFalse(wildcardMatch("-*.java, *.java, *.c", "hello.java"));
		assertTrue(wildcardMatch("+*.java, -*.java, *.c", "hello.java"));
	}

}
