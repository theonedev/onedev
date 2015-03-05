package com.pmease.commons.lang.tokenizer.lang.clike;

import org.junit.Test;

import com.pmease.commons.lang.tokenizer.AbstractTokenizerTest;

public class ScalaTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new ScalaTokenizer(), new String[]{"clike/clike.js"}, "test.scala");
	}

}
