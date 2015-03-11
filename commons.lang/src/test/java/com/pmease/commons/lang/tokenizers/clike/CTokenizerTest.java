package com.pmease.commons.lang.tokenizers.clike;

import org.junit.Test;

import com.pmease.commons.lang.tokenizers.AbstractTokenizerTest;
import com.pmease.commons.lang.tokenizers.clike.CTokenizer;

public class CTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new CTokenizer(), new String[]{"clike/clike.js"}, "test.c");
	}

}
