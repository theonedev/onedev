package com.gitplex.commons.lang.tokenizers.css;

import org.junit.Test;

import com.gitplex.commons.lang.tokenizers.css.LESSTokenizer;
import com.gitplex.commons.lang.tokenizers.AbstractTokenizerTest;

public class LESSTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new LESSTokenizer(), new String[]{"css/css.js"}, "test.less");
	}

}
