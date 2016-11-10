package com.gitplex.commons.lang.tokenizers.css;

import org.junit.Test;

import com.gitplex.commons.lang.tokenizers.css.SCSSTokenizer;
import com.gitplex.commons.lang.tokenizers.AbstractTokenizerTest;

public class SCSSTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new SCSSTokenizer(), new String[]{"css/css.js"}, "test.scss");
	}

}
