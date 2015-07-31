package com.pmease.commons.lang.diff;

import static org.junit.Assert.*;

import java.util.List;

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
		List<DiffBlock> diffBlocks = DiffUtils.diff(oldLines, "test.java", newLines, "test.java");
		assertEquals(""
				+ " public class HelloWorld {\n"
				+ " 	public static void main(String[] args) {\n", diffBlocks.get(0).toString());
		assertEquals(""
				+ "-		System.out.println(\"hello *robin*\");\n", diffBlocks.get(1).toString());
		assertEquals(""
				+ "+		System.out.println(\"hello *steve*\");\n", diffBlocks.get(2).toString());
		assertEquals(""
				+ " 	}\n"
				+ " }\n", diffBlocks.get(3).toString());
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
