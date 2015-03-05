package com.pmease.commons.lang.tokenizer.lang.css;

import org.junit.Test;

import com.pmease.commons.lang.tokenizer.AbstractTokenizerTest;

public class CssTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new CssTokenizer(), new String[]{"css/css.js"}, "test.css");
	}

}
