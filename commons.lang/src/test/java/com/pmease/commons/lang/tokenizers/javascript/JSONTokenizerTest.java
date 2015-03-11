package com.pmease.commons.lang.tokenizers.javascript;

import org.junit.Test;

import com.pmease.commons.lang.tokenizers.AbstractTokenizerTest;
import com.pmease.commons.lang.tokenizers.javascript.JSONTokenizer;

public class JSONTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new JSONTokenizer(), new String[]{"javascript/javascript.js"}, "test.json");
	}

}
