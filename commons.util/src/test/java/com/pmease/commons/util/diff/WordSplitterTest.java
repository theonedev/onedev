package com.pmease.commons.util.diff;

import static org.junit.Assert.assertArrayEquals;

import java.util.List;

import org.junit.Test;

public class WordSplitterTest {

	@Test
	public void test() {
		List<String> partials = new WordSplitter().split("hello world");
		assertArrayEquals(new String[]{"hello", " ", "world"}, partials.toArray());
		
		partials = new WordSplitter().split(" hello world ");
		assertArrayEquals(new String[]{" ", "hello", " ", "world", " "}, partials.toArray());

		partials = new WordSplitter().split("for(int i=0; i<100; i++)");
		assertArrayEquals(new String[]{"for", "(", "int", " ", "i", "=", "0", "; ", "i", "<", "100", "; ", "i", "++)"}, 
				partials.toArray());
	}

}
