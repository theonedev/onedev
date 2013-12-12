package com.pmease.commons.util.diff;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.pmease.commons.util.diff.DiffUnit.Action;

public class DiffUtilsTest {

	@Test
	public void shouldGenerateDiffUnitsCorrectly() {
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
		
		List<DiffUnit> diffUnits = DiffUtils.diff(text1, text2);
		assertEquals(13, diffUnits.size());
		
		assertEquals(Action.DELETE, diffUnits.get(1).getAction());
		assertEquals(Action.INSERT, diffUnits.get(2).getAction());
		assertEquals(Action.INSERT, diffUnits.get(12).getAction());
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
		
		List<DiffChunk> diffChunks = DiffUtils.diffAsChunks(text1, text2, 3);
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
		
		diffChunks = DiffUtils.diffAsChunks(text1, text2, 2);
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

}
