package com.pmease.commons.lang.tokenizer.lang.clike;

import org.junit.Test;

import com.pmease.commons.lang.tokenizer.AbstractTokenizerTest;

public class CTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new CTokenizer(), new String[]{"clike/clike.js"}, "test.c");
	}

}
