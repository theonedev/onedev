package com.pmease.commons.tokenizer;

import org.junit.Test;

public class CssTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new CssTokenizer.Css(), new String[]{"css/css.js"}, "testfiles/test.css");
		verify(new CssTokenizer.SCSS(), new String[]{"css/css.js"}, "testfiles/test.scss");
		verify(new CssTokenizer.LESS(), new String[]{"css/css.js"}, "testfiles/test.less");
	}

}
