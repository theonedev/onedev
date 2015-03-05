package com.pmease.commons.lang.tokenizer.lang.clike;

import org.junit.Test;

import com.pmease.commons.lang.tokenizer.AbstractTokenizerTest;

public class CppTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new CppTokenizer(), new String[]{"clike/clike.js"}, "test.cpp");
	}

}
