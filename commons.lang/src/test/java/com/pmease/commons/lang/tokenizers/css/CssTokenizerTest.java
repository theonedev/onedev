package com.pmease.commons.lang.tokenizers.css;

import org.junit.Test;

import com.pmease.commons.lang.tokenizers.AbstractTokenizerTest;
import com.pmease.commons.lang.tokenizers.css.CssTokenizer;

public class CssTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new CssTokenizer(), new String[]{"css/css.js"}, "test.css");
	}

}
