package com.pmease.commons.util.diff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Sets;
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
	public void shouldGenerateDiffHunksCorrectly() {
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
		
		List<DiffHunk> diffHunks = DiffUtils.diffAsHunks(text1, text2, null, 3);
		assertEquals(1, diffHunks.size());
		assertEquals(
				"@@ -1,10 +1,11 @@\n" +
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
				diffHunks.get(0).toString());
		
		diffHunks = DiffUtils.diffAsHunks(text1, text2, null, 2);
		assertEquals(2, diffHunks.size());
		assertEquals(
				"@@ -1,4 +1,4 @@\n" +
				" this is the 1st line\n" +
				"-this is the 2nd line\n" + 
				"+this is the second line\n" +
				" this is the 3rd line\n" +
				" this is the 4th line\n", 
				diffHunks.get(0).toString());
		assertEquals(
				"@@ -7,4 +7,5 @@\n" +
				" this is the 7th line\n" +
				" this is the 8th line\n" +
				"-this is the 9th line\n" + 
				"+this is the nineth line\n" +
				" this is the 10th line\n" +
				"+this is the 11th line\n", 
				diffHunks.get(1).toString());
		
		diffHunks = DiffUtils.diffAsHunks(text1, text2, null, new HashSet<Integer>(), Sets.newHashSet(4), 1);
		assertEquals(2, diffHunks.size());
		assertEquals(
				"@@ -1,6 +1,6 @@\n" +
				" this is the 1st line\n" +
				"-this is the 2nd line\n" + 
				"+this is the second line\n" +
				" this is the 3rd line\n" +
				" this is the 4th line\n" + 
				" this is the 5th line\n" +
				" this is the 6th line\n",
				diffHunks.get(0).toString());
		assertEquals(
				"@@ -8,3 +8,4 @@\n" +
				" this is the 8th line\n" +
				"-this is the 9th line\n" + 
				"+this is the nineth line\n" +
				" this is the 10th line\n" +
				"+this is the 11th line\n", 
				diffHunks.get(1).toString());

		diffHunks = DiffUtils.diffAsHunks(text1, text2, null, 0);
		assertEquals(3, diffHunks.size());
		assertEquals(2, diffHunks.get(0).getDiffLines().size());
		assertEquals(2, diffHunks.get(1).getDiffLines().size());
		assertEquals(1, diffHunks.get(2).getDiffLines().size());
		
		text1 = new ArrayList<>();
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

		text2 = new ArrayList<>();
		text2.add("this is the 1st line");
		text2.add("this is the 2nd line");
		text2.add("this is the 3rd line");
		text2.add("this is the 4th line");
		text2.add("this is the 5 line");
		text2.add("this is the 6th line");
		text2.add("this is the 7th line");
		text2.add("this is the 8th line");
		text2.add("this is the 9th line");
		text2.add("this is the 10th line");
		
		diffHunks = DiffUtils.diffAsHunks(text1, text2, null, 3);
		assertEquals(1, diffHunks.size());

		diffHunks = DiffUtils.diffAsHunks(text1, text2, null, 0);
		assertEquals(1, diffHunks.size());
		assertEquals(2, diffHunks.get(0).getDiffLines().size());
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
		
		
		List<DiffHunk> diffHunks = DiffUtils.diffAsHunks(text1, text2, new WordSplitter(), 100);
		assertEquals(
				"@@ -1,4 +1,5 @@\n" +
				" this is the 1st line\n" +
				"-this is the *2nd* line\n" + 
				"+this is the *11th* line\n" +
				"+this is the second line\n" +
				" this is the 3rd line\n" +
				" this is the 4th line\n", 
				diffHunks.get(0).toString());
		
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
		
		diffHunks = DiffUtils.diffAsHunks(text1, text2, new WordSplitter(), 100);
		assertEquals(
				"@@ -1,4 +1,5 @@\n" +
				" this is the 1st line\n" +
				"-this is the *2nd* line\n" + 
				"+*I** **do** **not** **think** *this is the *11th* line\n" +
				"+this is the second line\n" +
				" this is the 3rd line\n" +
				"-this is the 4th line\n" + 
				"+I do not think this works\n", 
				diffHunks.get(0).toString());
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
		assertTrue(map.get(0) == 0);
		assertTrue(map.get(1) == null);
		assertTrue(map.get(2) == 2);
		assertTrue(map.get(3) == 4);
		assertTrue(map.get(4) == 5);
		assertTrue(map.get(5) == 6);
	}
}
