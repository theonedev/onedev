package com.pmease.commons.util.diff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.pmease.commons.util.diff.DiffLine.Action;

public class DiffUtilsTest {

	@Test
	public void shouldGenerateDiffLinesCorrectly() {
		List<String> text1 = new ArrayList<>();
		text1.add("this is the 1st line");
		text1.add("this is the 2nd line");
		text1.add("this is the 3rd line");
		text1.add("this is the 4th line");
		text1.add("this is the 5th line");
		text1.add("this is the 6th line");
		text1.add("this is the 7th line");
		text1.add("this is the 8th line");
		text1.add("this is the 9th line");
		text1.add("this is the 10th line");

		List<String> text2 = new ArrayList<>();
		text2.add("this is the 1st line");
		text2.add("this is the second line");
		text2.add("this is the 3rd line");
		text2.add("this is the 4th line");
		text2.add("this is the 5th line");
		text2.add("this is the 6th line");
		text2.add("this is the 7th line");
		text2.add("this is the 8th line");
		text2.add("this is the nineth line");
		text2.add("this is the 10th line");
		text2.add("this is the 11th line");
		
		List<DiffLine> diffLines = DiffUtils.diff(text1, text2, null);
		assertEquals(13, diffLines.size());
		
		assertEquals(Action.DELETE, diffLines.get(1).getAction());
		assertEquals(Action.ADD, diffLines.get(2).getAction());
		assertEquals(Action.ADD, diffLines.get(12).getAction());
	}

	@Test
	public void shouldGenerateDiffChunksCorrectly() {
		List<String> text1 = new ArrayList<>();
		text1.add("this is the 1st line");
		text1.add("this is the 2nd line");
		text1.add("this is the 3rd line");
		text1.add("this is the 4th line");
		text1.add("this is the 5th line");
		text1.add("this is the 6th line");
		text1.add("this is the 7th line");
		text1.add("this is the 8th line");
		text1.add("this is the 9th line");
		text1.add("this is the 10th line");

		List<String> text2 = new ArrayList<>();
		text2.add("this is the 1st line");
		text2.add("this is the second line");
		text2.add("this is the 3rd line");
		text2.add("this is the 4th line");
		text2.add("this is the 5th line");
		text2.add("this is the 6th line");
		text2.add("this is the 7th line");
		text2.add("this is the 8th line");
		text2.add("this is the nineth line");
		text2.add("this is the 10th line");
		text2.add("this is the 11th line");
		
		List<DiffChunk> diffChunks = DiffUtils.diffAsChunks(text1, text2, null, 3);
		assertEquals(1, diffChunks.size());
		assertEquals(
				"@@ -1 +1 @@\n" +
				" this is the 1st line\n" +
				"-this is the 2nd line\n" +
				"+this is the second line\n" +
				" this is the 3rd line\n" +
				" this is the 4th line\n" +
				" this is the 5th line\n" +
				" this is the 6th line\n" +
				" this is the 7th line\n" +
				" this is the 8th line\n" +
				"-this is the 9th line\n" +
				"+this is the nineth line\n" +
				" this is the 10th line\n" +
				"+this is the 11th line\n",
				diffChunks.get(0).toString());
		
		diffChunks = DiffUtils.diffAsChunks(text1, text2, null, 2);
		assertEquals(2, diffChunks.size());
		assertEquals(
				"@@ -1 +1 @@\n" +
				" this is the 1st line\n" +
				"-this is the 2nd line\n" + 
				"+this is the second line\n" +
				" this is the 3rd line\n" +
				" this is the 4th line\n", 
				diffChunks.get(0).toString());
		assertEquals(
				"@@ -7 +7 @@\n" +
				" this is the 7th line\n" +
				" this is the 8th line\n" +
				"-this is the 9th line\n" + 
				"+this is the nineth line\n" +
				" this is the 10th line\n" +
				"+this is the 11th line\n", 
				diffChunks.get(1).toString());
	}

	@Test
	public void shouldEmphasizeDiffWordsCorrectly() {
		List<String> text1 = new ArrayList<>();
		text1.add("this is the 1st line");
		text1.add("this is the 2nd line");
		text1.add("this is the 3rd line");
		text1.add("this is the 4th line");

		List<String> text2 = new ArrayList<>();
		text2.add("this is the 1st line");
		text2.add("this is the 11th line");
		text2.add("this is the second line");
		text2.add("this is the 3rd line");
		text2.add("this is the 4th line");
		
		List<DiffChunk> diffChunks = DiffUtils.diffAsChunks(text1, text2, new WordSplitter(), 100);
		assertEquals(
				"@@ -1 +1 @@\n" +
				" this is the 1st line\n" +
				"-this is the *2nd* line\n" + 
				"+this is the *11th* line\n" +
				"+this is the second line\n" +
				" this is the 3rd line\n" +
				" this is the 4th line\n", 
				diffChunks.get(0).toString());
		
		text1 = new ArrayList<>();
		text1.add("this is the 1st line");
		text1.add("this is the 2nd line");
		text1.add("this is the 3rd line");
		text1.add("this is the 4th line");

		text2 = new ArrayList<>();
		text2.add("this is the 1st line");
		text2.add("I do not think this is the 11th line");
		text2.add("this is the second line");
		text2.add("this is the 3rd line");
		text2.add("I do not think this works");
		
		diffChunks = DiffUtils.diffAsChunks(text1, text2, new WordSplitter(), 100);
		assertEquals(
				"@@ -1 +1 @@\n" +
				" this is the 1st line\n" +
				"-this is the *2nd* line\n" + 
				"+I do not think this is the 11th line\n" +
				"+this is the *second* line\n" +
				" this is the 3rd line\n" +
				"-this is the 4th line\n" + 
				"+I do not think this works\n", 
				diffChunks.get(0).toString());
		
	}

	@Test
	public void testMapLines() {
		String[] original = new String[]{
				"line 1",
				"line 2",
				"line 3",
				"line 4",
				"line 5",
				"line 6"
		};
		String[] revised = new String[]{
				"line 1", 
				"line second",
				"line 3",
				"line 3.1",
				"line 4",
				"line 5",
				"line 6"
		};
		Map<Integer, Integer> map = DiffUtils.mapLines(Arrays.asList(original), Arrays.asList(revised));
		assertTrue(map.get(1) == 1);
		assertTrue(map.get(2) == null);
		assertTrue(map.get(3) == 3);
		assertTrue(map.get(4) == 5);
		assertTrue(map.get(5) == 6);
		assertTrue(map.get(6) == 7);
	}
}
