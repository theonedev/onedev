package com.pmease.commons.lang.tokenizer.lang.css;

import org.junit.Test;

import com.pmease.commons.lang.tokenizer.AbstractTokenizerTest;

public class SCSSTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new SCSSTokenizer(), new String[]{"css/css.js"}, "test.scss");
	}

}
