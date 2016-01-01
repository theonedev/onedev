package com.pmease.commons.util.pattern;

import static org.junit.Assert.*;
import static com.pmease.commons.util.pattern.WildcardUtils.*;

import org.junit.Test;

public class WildcardUtilsTest {

	@Test
	public void shouldMatchPath() {
		assertTrue(matchPath("-*.java", "file.c"));
		assertTrue(matchPath("**//*.java", "com/example/Test.java"));
		assertFalse(matchPath("**/*.java", "com/example/test.c"));
		assertFalse(matchPath("com/*.java", "com/example/Test.java"));
		assertTrue(matchPath("com/**//*.java", "com/example/Test.java"));
		assertTrue(matchPath("com/*.java", "com/Test.java"));
		assertTrue(matchPath("src///**/*.java", "src/com/example/Test.java"));
		assertFalse(matchPath("/src/**/*.java", "src/com/example/Test.java"));
		assertFalse(matchPath("src/**////*.java", "resource/com/example/Test.java"));

		assertTrue(matchPath("-**/*.java, **", "test/hello.c"));
		assertFalse(matchPath("-**/*.java, **", "test/hello.java"));
		assertFalse(matchPath("-**/*.java, **/*.java, **/*.c", "test/hello.txt"));
		assertFalse(matchPath("-**/*.java, **/*.java, **/*.c", "test/hello.java"));
		assertTrue(matchPath("+**/*.java, -**/*.java, **/*.c", "test/hello.java"));
		assertTrue(matchPath("-**/generated/**, **/*.xml", "test/test.xml"));		
		assertFalse(matchPath("-**/generated/**, **/*.xml", "generated/test.xml"));		
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
		
		assertTrue(matchString("-*.java", "hello.c"));
		assertFalse(matchString("-*.java", "hello.java"));
		assertTrue(matchString("-*.java, *", "hello.c"));
		assertFalse(matchString("-*.java, *", "hello.java"));
		assertFalse(matchString("-*.java, *.java, *.c", "hello.txt"));
		assertFalse(matchString("-*.java, *.java, *.c", "hello.java"));
		assertTrue(matchString("+*.java, -*.java, *.c", "hello.java"));
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
