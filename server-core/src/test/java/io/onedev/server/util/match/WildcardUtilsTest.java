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
	public void shouldApplyStringPattern() {
		assertEquals("he*?lo:1-5", applyStringPattern("E*?l", "hello", false).toString());
		assertEquals(null, applyStringPattern("*he ?d", "hello world", false));
		assertEquals("*he*ld 2:0-6", applyStringPattern("*hE*ld", "hello world 2", false).toString());
		assertEquals("hello:0-0", applyStringPattern("", "hello", false).toString());
		assertEquals("*:0-1", applyStringPattern("*", "hello", false).toString());
		assertEquals("**:0-2", applyStringPattern("**", "hello", false).toString());
		assertEquals("he*:0-3", applyStringPattern("he*", "hello", false).toString());
	}
	
	@Test
	public void shouldApplyPathPattern() {
		assertEquals(null, applyPathPattern("ir/**/c/*.java", "dir/a/b/file.java", false));
		assertEquals("dir/file:4-7", applyPathPattern("fil", "dir/file", false).toString());
		assertEquals("dir/file:3-4", applyPathPattern("/", "dir/file", false).toString());
		assertEquals("dir/**/c*/*.java:1-16", applyPathPattern("ir/**/c*/*.java", "dir/a/b/c5/file.java", false).toString());
		assertEquals("dir/**/*.java:1-13", applyPathPattern("ir/**/*.java", "dir/a/b/file.java", false).toString());
		assertEquals("dir/**/file:0-11", applyPathPattern("dir/**/file", "dir/file", false).toString());
		assertEquals("dir/**/file:0-11", applyPathPattern("dir/**/file", "dir/a/b/file", false).toString());
		assertEquals("**/file:0-7", applyPathPattern("**/file", "dir/a/b/file", false).toString());
		assertEquals("**/file:0-7", applyPathPattern("**/file", "file", false).toString());
		assertEquals("dir/**:0-6", applyPathPattern("dir/**", "dir/subdir/file", false).toString());
		assertEquals("dir/**:0-6", applyPathPattern("dir/**", "dir/", false).toString());
		assertEquals("dir/**:0-6", applyPathPattern("dir/**", "dir", false).toString());
		assertEquals("di*/file:1-4", applyPathPattern("i*/", "dir/file", false).toString());
		assertEquals("dir/file:1-4", applyPathPattern("ir/", "dir/file", false).toString());
		assertEquals(null, applyPathPattern("**", "/file", false));
		assertEquals("*:0-1", applyPathPattern("*", "dir", false).toString());
		assertEquals("*/file:0-1", applyPathPattern("*", "dir/file", false).toString());
		assertEquals("**:0-2", applyPathPattern("**", "dir/file", false).toString());
	}
	
}
