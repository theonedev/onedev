package com.pmease.commons.lang.tokenizers.clike;

import org.junit.Test;

import com.pmease.commons.lang.tokenizers.AbstractTokenizerTest;
import com.pmease.commons.lang.tokenizers.clike.CSharpTokenizer;

public class CSharpTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new CSharpTokenizer(), new String[]{"clike/clike.js"}, "test.cs");
	}

}
