package com.gitplex.commons.lang.tokenizers.javascript;

import org.junit.Test;

import com.gitplex.commons.lang.tokenizers.javascript.JSONTokenizer;
import com.gitplex.commons.lang.tokenizers.AbstractTokenizerTest;

public class JSONTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new JSONTokenizer(), new String[]{"javascript/javascript.js"}, "test.json");
	}

}
