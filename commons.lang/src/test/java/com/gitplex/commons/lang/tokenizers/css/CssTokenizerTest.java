package com.gitplex.commons.lang.tokenizers.css;

import org.junit.Test;

import com.gitplex.commons.lang.tokenizers.css.CssTokenizer;
import com.gitplex.commons.lang.tokenizers.AbstractTokenizerTest;

public class CssTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new CssTokenizer(), new String[]{"css/css.js"}, "test.css");
		verify(new CssTokenizer(), new String[]{"css/css.js"}, "test2.css");
	}

}
