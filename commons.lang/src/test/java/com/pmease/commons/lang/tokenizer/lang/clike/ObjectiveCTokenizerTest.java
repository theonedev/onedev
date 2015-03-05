package com.pmease.commons.lang.tokenizer.lang.clike;

import org.junit.Test;

import com.pmease.commons.lang.tokenizer.AbstractTokenizerTest;

public class ObjectiveCTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new ObjectiveCTokenizer(), new String[]{"clike/clike.js"}, "test.m");
	}

}
