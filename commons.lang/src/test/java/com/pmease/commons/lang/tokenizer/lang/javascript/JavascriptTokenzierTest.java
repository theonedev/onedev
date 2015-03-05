package com.pmease.commons.lang.tokenizer.lang.javascript;

import org.junit.Test;

import com.pmease.commons.lang.tokenizer.AbstractTokenizerTest;
import com.pmease.commons.lang.tokenizer.lang.ecmascript.JavaScriptTokenizer;

public class JavascriptTokenzierTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new JavaScriptTokenizer(), new String[]{"javascript/javascript.js"}, "test.js");
	}

}
