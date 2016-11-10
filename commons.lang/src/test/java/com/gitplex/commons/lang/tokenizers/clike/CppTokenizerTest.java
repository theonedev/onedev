package com.gitplex.commons.lang.tokenizers.clike;

import org.junit.Test;

import com.gitplex.commons.lang.tokenizers.clike.CppTokenizer;
import com.gitplex.commons.lang.tokenizers.AbstractTokenizerTest;

public class CppTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new CppTokenizer(), new String[]{"clike/clike.js"}, "test.cpp");
	}

}
