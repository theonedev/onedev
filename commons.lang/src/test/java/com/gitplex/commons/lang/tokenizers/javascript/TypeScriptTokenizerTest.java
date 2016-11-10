package com.gitplex.commons.lang.tokenizers.javascript;

import org.junit.Test;

import com.gitplex.commons.lang.tokenizers.javascript.TypeScriptTokenizer;
import com.gitplex.commons.lang.tokenizers.AbstractTokenizerTest;

public class TypeScriptTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new TypeScriptTokenizer(), new String[]{"javascript/javascript.js"}, "test.ts");
	}

}
