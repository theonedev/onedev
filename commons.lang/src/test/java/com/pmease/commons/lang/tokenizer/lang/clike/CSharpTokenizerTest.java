package com.pmease.commons.lang.tokenizer.lang.clike;

import org.junit.Test;

import com.pmease.commons.lang.tokenizer.AbstractTokenizerTest;

public class CSharpTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new CSharpTokenizer(), new String[]{"clike/clike.js"}, "test.cs");
	}

}
