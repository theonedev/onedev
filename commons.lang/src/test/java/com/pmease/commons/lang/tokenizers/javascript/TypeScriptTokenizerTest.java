package com.pmease.commons.lang.tokenizers.javascript;

import org.junit.Test;

import com.pmease.commons.lang.tokenizers.AbstractTokenizerTest;
import com.pmease.commons.lang.tokenizers.javascript.TypeScriptTokenizer;

public class TypeScriptTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new TypeScriptTokenizer(), new String[]{"javascript/javascript.js"}, "test.ts");
	}

}
