package com.pmease.commons.util.pattern;

import static org.junit.Assert.*;
import static com.pmease.commons.util.pattern.WildcardUtils.*;

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

		assertTrue(matchPath("-**/*.java, **", "test/hello.c"));
		assertFalse(matchPath("-**/*.java, **", "test/hello.java"));
		assertFalse(matchPath("-**/*.java, **/*.java, **/*.c", "test/hello.txt"));
		assertFalse(matchPath("-**/*.java, **/*.java, **/*.c", "test/hello.java"));
		assertTrue(matchPath("+**/*.java, -**/*.java, **/*.c", "test/hello.java"));
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
		
		assertFalse(matchString("-*.java", "hello.c"));
		assertFalse(matchString("-*.java", "hello.java"));
		assertTrue(matchString("-*.java, *", "hello.c"));
		assertFalse(matchString("-*.java, *", "hello.java"));
		assertFalse(matchString("-*.java, *.java, *.c", "hello.txt"));
		assertFalse(matchString("-*.java, *.java, *.c", "hello.java"));
		assertTrue(matchString("+*.java, -*.java, *.c", "hello.java"));
	}

}
