package com.pmease.commons.lang.tokenizers.javascript;

import org.junit.Test;

import com.pmease.commons.lang.tokenizers.AbstractTokenizerTest;
import com.pmease.commons.lang.tokenizers.javascript.JavaScriptTokenizer;

public class JavascriptTokenzierTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new JavaScriptTokenizer(), new String[]{"javascript/javascript.js"}, "test.js");
	}

}
