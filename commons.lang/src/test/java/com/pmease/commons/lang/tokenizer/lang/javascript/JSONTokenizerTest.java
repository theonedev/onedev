package com.pmease.commons.lang.tokenizer.lang.javascript;

import org.junit.Test;

import com.pmease.commons.lang.tokenizer.AbstractTokenizerTest;
import com.pmease.commons.lang.tokenizer.lang.ecmascript.JSONTokenizer;

public class JSONTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new JSONTokenizer(), new String[]{"javascript/javascript.js"}, "test.json");
	}

}
