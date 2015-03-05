package com.pmease.commons.lang.tokenizer.lang.css;

import org.junit.Test;

import com.pmease.commons.lang.tokenizer.AbstractTokenizerTest;

public class LESSTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new LESSTokenizer(), new String[]{"css/css.js"}, "test.less");
	}

}
