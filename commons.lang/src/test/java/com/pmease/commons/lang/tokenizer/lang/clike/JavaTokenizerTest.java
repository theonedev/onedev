package com.pmease.commons.lang.tokenizer.lang.clike;

import org.junit.Test;

import com.pmease.commons.lang.tokenizer.AbstractTokenizerTest;

public class JavaTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new JavaTokenizer(), new String[]{"clike/clike.js"}, "test.java.txt");
	}

}
