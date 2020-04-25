package io.onedev.server.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PathComparatorTest {

	@Test
	public void test() {
		assertEquals(new PathComparator().compare("a/b", "b"), -1);
		assertEquals(new PathComparator().compare("a", "b"), -1);
		assertEquals(new PathComparator().compare("a/c", "a"), 1);
		assertEquals(new PathComparator().compare("abc", "ab/c"), 1);
		assertEquals(new PathComparator().compare("a/b/c", "a/b/c"), 0);
		assertEquals(new PathComparator().compare("a/b/c/d", "a/b/c"), 1);
	}

}
