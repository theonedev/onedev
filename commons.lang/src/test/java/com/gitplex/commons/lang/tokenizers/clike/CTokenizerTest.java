package com.gitplex.commons.lang.tokenizers.clike;

import org.junit.Test;

import com.gitplex.commons.lang.tokenizers.clike.CTokenizer;
import com.gitplex.commons.lang.tokenizers.AbstractTokenizerTest;

public class CTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new CTokenizer(), new String[]{"clike/clike.js"}, "test.c");
	}

}
