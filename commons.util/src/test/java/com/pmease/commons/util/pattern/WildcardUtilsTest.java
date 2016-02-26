package com.pmease.commons.util.pattern;

import static com.pmease.commons.util.match.WildcardUtils.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class WildcardUtilsTest {

	@Test
	public void shouldMatchPath() {
		assertTrue(matchPath("**//*.java", "com/example/Test.java"));
		assertFalse(matchPath("**/*.java", "com/example/test.c"));
		assertFalse(matchPath("com/*.java", "com/example/Test.java"));
		assertTrue(matchPath("com/**//*.java", "com/example/Test.java"));
		assertTrue(matchPath("com/*.java", "com/Test.java"));
		assertTrue(matchPath("src///**/*.java", "src/com/example/Test.java"));
		assertFalse(matchPath("/src/**/*.java", "src/com/example/Test.java"));
		assertFalse(matchPath("src/**////*.java", "resource/com/example/Test.java"));
	}
	
	@Test
	public void shouldMatchString() {
		assertTrue(matchString("hello?world", "hello world"));
		assertFalse(matchString("hello?world", "hello  world"));
		assertTrue(matchString("hello*world", "hello  world"));
		assertTrue(matchString("**/*.java", "com/example/Test.java"));
		assertFalse(matchString("**/*.java", "com/example/test.c"));
		assertTrue(matchString("com/*.java", "com/example/Test.java"));
		assertTrue(matchString("com/**/*.java", "com/example/Test.java"));
		assertTrue(matchString("com/*.java", "com/Test.java"));
		assertTrue(matchString("src/**/*.java", "src/com/example/Test.java"));
		assertFalse(matchString("src/**/*.java", "resource/com/example/Test.java"));
	}

	@Test
	public void shouldApplyWildcard() {
		assertEquals("he*llo:1-4", applyWildcard("hello", "e*l", false).toString());
		assertEquals(null, applyWildcard("hello world", "*he ld", false));
		assertEquals("*he*ld 2:0-6", applyWildcard("hello world 2", "*he*ld", false).toString());
		assertEquals("hello:0-0", applyWildcard("hello", "", false).toString());
		assertEquals("*:0-1", applyWildcard("hello", "*", false).toString());
		assertEquals("**:0-2", applyWildcard("hello", "**", false).toString());
	}
}
