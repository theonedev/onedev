package com.pmease.commons.lang.diff;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.pmease.commons.lang.tokenizers.CmToken;
import com.pmease.commons.lang.tokenizers.Tokenizers;
import com.pmease.commons.lang.tokenizers.clike.JavaTokenizer;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.AppLoaderMocker;

public class DiffUtilsTest extends AppLoaderMocker {

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
		List<DiffBlock> diffBlocks = DiffUtils.diff(oldLines, "test.java", newLines, "test.java");
		assertEquals(""
				+ "-public class *HelloRobin* {\n", diffBlocks.get(0).toString());
		assertEquals(""
				+ "+package test;\n"
				+ "+public class *HelloTim* {\n", diffBlocks.get(1).toString());
		assertEquals(""
				+ " 	public static void main(String[] args) {\n", diffBlocks.get(2).toString());
		assertEquals(""
				+ "-		System.out.println(\"hello *robin*\");\n", diffBlocks.get(3).toString());
		assertEquals(""
				+ "+		System.out.println(\"hello *tim*\");\n", diffBlocks.get(4).toString());
		assertEquals(""
				+ " 	}\n"
				+ " }\n", diffBlocks.get(5).toString());
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
	public void testAround() {
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
		List<DiffBlock> diffBlocks = DiffUtils.diff(oldLines, "test.java", newLines, "test.java");
		AroundContext aroundContext = DiffUtils.around(diffBlocks, 2, -1, 2);
		assertEquals("+public class *HelloTim* {", 
				aroundContext.getDiffLines().get(0).toString());
		assertEquals(" 	public static void main(String[] args) {", 
				aroundContext.getDiffLines().get(1).toString());
		assertEquals("-		System.out.println(\"hello *robin*\");", 
				aroundContext.getDiffLines().get(2).toString());
		assertEquals("+		System.out.println(\"hello *tim*\");", 
				aroundContext.getDiffLines().get(3).toString());
		assertEquals(" 	}", 
				aroundContext.getDiffLines().get(4).toString());
	}
	
	@Test
	public void testSimpleDiff() {
		List<String> oldLines = Lists.newArrayList(
				"public class HelloWorld {",
				"	public static void main(String[] args) {",
				"		System.out.println(\"hello robin\");",
				"	}",
				"}");
		List<String> newLines = Lists.newArrayList(
				"public class HelloWorld {",
				"	public static void main(String[] args) {",
				"		System.out.println(\"hello steve\");",
				"	}",
				"}");
		List<SimpleDiffBlock> diffBlocks = DiffUtils.simpleDiff(oldLines, newLines);
		assertEquals(""
				+ " public class HelloWorld {\n"
				+ " 	public static void main(String[] args) {\n", diffBlocks.get(0).toString());
		assertEquals(""
				+ "-		System.out.println(\"hello robin\");\n", diffBlocks.get(1).toString());
		assertEquals(""
				+ "+		System.out.println(\"hello steve\");\n", diffBlocks.get(2).toString());
		assertEquals(""
				+ " 	}\n"
				+ " }\n", diffBlocks.get(3).toString());
		assertEquals(0, diffBlocks.get(0).getOldStart());
		assertEquals(0, diffBlocks.get(0).getNewStart());
		assertEquals(2, diffBlocks.get(1).getOldStart());
		assertEquals(2, diffBlocks.get(1).getNewStart());
		assertEquals(3, diffBlocks.get(2).getOldStart());
		assertEquals(2, diffBlocks.get(2).getNewStart());
		assertEquals(3, diffBlocks.get(3).getOldStart());
		assertEquals(3, diffBlocks.get(3).getNewStart());
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
	
	@Override
	protected void setup() {
		Mockito.when(AppLoader.getInstance(Tokenizers.class)).thenReturn(new Tokenizers() {

			@Override
			public List<List<CmToken>> tokenize(List<String> lines, String fileName) {
				return new JavaTokenizer().tokenize(lines);
			}
			
		});
	}

	@Override
	protected void teardown() {
	}

}
