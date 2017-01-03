package com.gitplex.commons.lang.diff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.gitplex.commons.lang.diff.DiffMatchPatch.Operation;
import com.gitplex.jsyntax.Token;
import com.google.common.collect.Lists;

public class DiffUtilsTest {

	private String toString(DiffBlock<List<Token>> block) {
		StringBuffer buffer = new StringBuffer();
		for (List<Token> line: block.getUnits()) {
			if (block.getOperation() == Operation.INSERT)
				buffer.append("+");
			else if (block.getOperation() == Operation.DELETE)
				buffer.append("-");
			else
				buffer.append(" ");
			for (Token token: line) 
				buffer.append(token.getText());
			buffer.append("\n");
		}
		return buffer.toString();
	}
	
	@Test
	public void testDiff() {
		List<String> oldLines = Lists.newArrayList(
				"public class HelloRobin {",
				"	public static void main(String[] args) {",
				"		System.out.println(\"hello robin\");",
				"	}",
				"}");
		List<String> newLines = Lists.newArrayList(
				"package test;",
				"public class HelloTim {",
				"	public static void main(String[] args) {",
				"		System.out.println(\"hello tim\");",
				"	}",
				"}");
		List<DiffBlock<List<Token>>> diffBlocks = DiffUtils.diff(
				oldLines, "test.java", 
				newLines, "test.java", 
				WhitespaceOption.DEFAULT);
		assertEquals(""
				+ "-public class HelloRobin {\n", toString(diffBlocks.get(0)));
		assertEquals(""
				+ "+package test;\n"
				+ "+public class HelloTim {\n", toString(diffBlocks.get(1)));
		assertEquals(""
				+ " 	public static void main(String[] args) {\n", toString(diffBlocks.get(2)));
		assertEquals(""
				+ "-		System.out.println(\"hello robin\");\n", toString(diffBlocks.get(3)));
		assertEquals(""
				+ "+		System.out.println(\"hello tim\");\n", toString(diffBlocks.get(4)));
		assertEquals(""
				+ " 	}\n"
				+ " }\n", toString(diffBlocks.get(5)));
		assertEquals(0, diffBlocks.get(0).getOldStart());
		assertEquals(0, diffBlocks.get(0).getNewStart());
		assertEquals(1, diffBlocks.get(1).getOldStart());
		assertEquals(0, diffBlocks.get(1).getNewStart());
		assertEquals(1, diffBlocks.get(2).getOldStart());
		assertEquals(2, diffBlocks.get(2).getNewStart());
		assertEquals(2, diffBlocks.get(3).getOldStart());
		assertEquals(3, diffBlocks.get(3).getNewStart());
		assertEquals(3, diffBlocks.get(4).getOldStart());
		assertEquals(3, diffBlocks.get(4).getNewStart());
		assertEquals(3, diffBlocks.get(5).getOldStart());
		assertEquals(4, diffBlocks.get(5).getNewStart());
	}

	@Test
	public void testMapLines() {
		List<String> oldLines = Lists.newArrayList(
				"line 1",
				"line 2",
				"line 3",
				"line 4",
				"line 5",
				"line 6"
		);
		List<String> newLines = Lists.newArrayList(
				"line 1", 
				"line second",
				"line 3",
				"line 3.1",
				"line 4",
				"line 5",
				"line 6"
		);
		Map<Integer, Integer> map = DiffUtils.mapLines(oldLines, newLines);
		assertTrue(map.get(0) == 0);
		assertTrue(map.get(1) == null);
		assertTrue(map.get(2) == 2);
		assertTrue(map.get(3) == 4);
		assertTrue(map.get(4) == 5);
		assertTrue(map.get(5) == 6);
	}

}
