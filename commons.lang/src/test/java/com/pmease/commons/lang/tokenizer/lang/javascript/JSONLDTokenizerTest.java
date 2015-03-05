package com.pmease.commons.lang.tokenizer.lang.javascript;

import org.junit.Test;

import com.pmease.commons.lang.tokenizer.AbstractTokenizerTest;
import com.pmease.commons.lang.tokenizer.lang.ecmascript.JSONLDTokenizer;

public class JSONLDTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new JSONLDTokenizer(), new String[]{"javascript/javascript.js"}, "test.jsonld");
	}

}
