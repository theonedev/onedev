package io.onedev.server.util.diff;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class DiffUtilsTest extends DiffUtils {

	@Test
	public void testIsVisible() {
		List<String> oldLines = Lists.newArrayList("1", "2", "3", "4a", "5a", "6", "7", "8");
		List<String> newLines = Lists.newArrayList("1", "2", "3", "4b", "5b", "7", "8", "9", "10");
		
		List<DiffBlock<String>> diffBlocks = DiffUtils.diff(oldLines, newLines);
		
		for (int i=0; i<oldLines.size(); i++)
			assertTrue(DiffUtils.isVisible(diffBlocks, true, i, 3));
		for (int i=0; i<newLines.size(); i++)
			assertTrue(DiffUtils.isVisible(diffBlocks, false, i, 3));
		
		assertFalse(DiffUtils.isVisible(diffBlocks, true, 0, 1));
		assertFalse(DiffUtils.isVisible(diffBlocks, true, 1, 1));
		assertTrue(DiffUtils.isVisible(diffBlocks, true, 2, 1));
		assertTrue(DiffUtils.isVisible(diffBlocks, true, 2, 1));
		assertTrue(DiffUtils.isVisible(diffBlocks, true, 7, 1));
		assertFalse(DiffUtils.isVisible(diffBlocks, true, 8, 1));
		assertTrue(DiffUtils.isVisible(diffBlocks, false, 6, 1));
		assertTrue(DiffUtils.isVisible(diffBlocks, false, 7, 1));
		
		oldLines = Lists.newArrayList(
				"1", "2", "3", "4", "5", "6a", "7a", "8", "9", "10", 
				"11", "12", "13", "14", "15a", "16", "17", "18", "19", "20");
		newLines = Lists.newArrayList(
				"1", "2", "3", "4", "5", "6b", "7b", "8", "9", "10", 
				"11", "12", "13", "14", "15b", "16", "17", "18", "19", "20");
		diffBlocks = DiffUtils.diff(oldLines, newLines);
		
		assertFalse(DiffUtils.isVisible(diffBlocks, true, 1, 3));
		assertTrue(DiffUtils.isVisible(diffBlocks, true, 9, 3));
		assertFalse(DiffUtils.isVisible(diffBlocks, true, 10, 3));
		assertTrue(DiffUtils.isVisible(diffBlocks, true, 11, 3));
		assertTrue(DiffUtils.isVisible(diffBlocks, true, 17, 3));
		assertFalse(DiffUtils.isVisible(diffBlocks, true, 18, 3));
		
		assertFalse(DiffUtils.isVisible(diffBlocks, false, 1, 3));
		assertTrue(DiffUtils.isVisible(diffBlocks, false, 9, 3));
		assertFalse(DiffUtils.isVisible(diffBlocks, false, 10, 3));
		assertTrue(DiffUtils.isVisible(diffBlocks, false, 11, 3));
		assertTrue(DiffUtils.isVisible(diffBlocks, false, 17, 3));
		assertFalse(DiffUtils.isVisible(diffBlocks, false, 18, 3));
	}

}
