package com.gitplex.commons.lang.tokenizers.javascript;

import org.junit.Test;

import com.gitplex.commons.lang.tokenizers.javascript.JSONLDTokenizer;
import com.gitplex.commons.lang.tokenizers.AbstractTokenizerTest;

public class JSONLDTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new JSONLDTokenizer(), new String[]{"javascript/javascript.js"}, "test.jsonld");
	}

}
