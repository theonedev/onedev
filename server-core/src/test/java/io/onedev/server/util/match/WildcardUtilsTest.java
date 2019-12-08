package io.onedev.server.util.match;

import static io.onedev.server.util.match.WildcardUtils.*;
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
	public void testRangeOfMatch() {
		assertEquals("1-3", rangeOfMatch("el", "hello").toString());
		assertEquals(null, rangeOfMatch("tu", "hello"));
		assertEquals("3-8", rangeOfMatch("lo*wo", "hello world").toString());
		assertEquals("3-9", rangeOfMatch("lo*w?r", "hello world").toString());
		assertEquals("12-37", rangeOfMatch("this*my*program", "hello world this is  my first program").toString());
	}
	
	@Test
	public void shouldApplyPattern() {
		assertEquals("he*?lo:1-5", applyPattern("E*?l", "hello", false).toString());
		assertEquals(null, applyPattern("*he ?d", "hello world", false));
		assertEquals("*he*ld 2:0-6", applyPattern("*hE*ld", "hello world 2", false).toString());
		assertEquals("hello:0-0", applyPattern("", "hello", false).toString());
		assertEquals("*:0-1", applyPattern("*", "hello", false).toString());
		assertEquals("**:0-2", applyPattern("**", "hello", false).toString());
	}
	
}
