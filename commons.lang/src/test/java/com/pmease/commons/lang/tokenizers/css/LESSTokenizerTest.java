package com.pmease.commons.lang.tokenizers.css;

import org.junit.Test;

import com.pmease.commons.lang.tokenizers.AbstractTokenizerTest;
import com.pmease.commons.lang.tokenizers.css.LESSTokenizer;

public class LESSTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new LESSTokenizer(), new String[]{"css/css.js"}, "test.less");
	}

}
