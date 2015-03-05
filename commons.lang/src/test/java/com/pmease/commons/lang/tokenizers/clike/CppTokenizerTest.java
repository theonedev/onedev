package com.pmease.commons.lang.tokenizers.clike;

import org.junit.Test;

import com.pmease.commons.lang.tokenizers.AbstractTokenizerTest;
import com.pmease.commons.lang.tokenizers.clike.CppTokenizer;

public class CppTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new CppTokenizer(), new String[]{"clike/clike.js"}, "test.cpp");
	}

}
