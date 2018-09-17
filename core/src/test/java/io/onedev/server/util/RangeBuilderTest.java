package io.onedev.server.util;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class RangeBuilderTest {

	@Test
	public void test() {
		List<List<Long>> ranges = new RangeBuilder(Lists.newArrayList(2L, 3L, 6L, 8L, 9L), Lists.newArrayList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)).getRanges();
		assertEquals(3, ranges.size());
		assertEquals(Lists.newArrayList(2L, 3L), ranges.get(0));
		assertEquals(Lists.newArrayList(6L), ranges.get(1));
		assertEquals(Lists.newArrayList(8L, 9L), ranges.get(2));
		
		ranges = new RangeBuilder(Lists.newArrayList(1L, 3L, 4L, 9L), Lists.newArrayList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)).getRanges();
		assertEquals(3, ranges.size());
		assertEquals(Lists.newArrayList(1L), ranges.get(0));
		assertEquals(Lists.newArrayList(3L, 4L), ranges.get(1));
		assertEquals(Lists.newArrayList(9L), ranges.get(2));
		
		ranges = new RangeBuilder(Lists.newArrayList(10L, 30L, 40L, 5L, 90L), Lists.newArrayList(10L, 20L, 30L, 40L, 50L, 60L, 70L, 80L, 90L, 100L)).getRanges();
		assertEquals(3, ranges.size());
		assertEquals(Lists.newArrayList(10L), ranges.get(0));
		assertEquals(Lists.newArrayList(30L, 40L), ranges.get(1));
		assertEquals(Lists.newArrayList(90L), ranges.get(2));
		
	}

}
