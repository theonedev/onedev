package com.gitplex.commons.lang.tokenizers.javascript;

import org.junit.Test;

import com.gitplex.commons.lang.tokenizers.javascript.JavaScriptTokenizer;
import com.gitplex.commons.lang.tokenizers.AbstractTokenizerTest;

public class JavascriptTokenzierTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new JavaScriptTokenizer(), new String[]{"javascript/javascript.js"}, "test.js");
		verify(new JavaScriptTokenizer(), new String[]{"javascript/javascript.js"}, "test2.js");
	}

}
