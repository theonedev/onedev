package com.pmease.commons.lang.tokenizer.lang.javascript;

import org.junit.Test;

import com.pmease.commons.lang.tokenizer.AbstractTokenizerTest;
import com.pmease.commons.lang.tokenizer.lang.ecmascript.TypeScriptTokenizer;

public class TypeScriptTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new TypeScriptTokenizer(), new String[]{"javascript/javascript.js"}, "test.ts");
	}

}
