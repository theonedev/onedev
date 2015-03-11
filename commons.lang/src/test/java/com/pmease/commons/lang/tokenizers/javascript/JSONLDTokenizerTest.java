package com.pmease.commons.lang.tokenizers.javascript;

import org.junit.Test;

import com.pmease.commons.lang.tokenizers.AbstractTokenizerTest;
import com.pmease.commons.lang.tokenizers.javascript.JSONLDTokenizer;

public class JSONLDTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new JSONLDTokenizer(), new String[]{"javascript/javascript.js"}, "test.jsonld");
	}

}
