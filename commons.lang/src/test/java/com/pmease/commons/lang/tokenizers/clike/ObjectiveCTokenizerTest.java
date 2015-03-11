package com.pmease.commons.lang.tokenizers.clike;

import org.junit.Test;

import com.pmease.commons.lang.tokenizers.AbstractTokenizerTest;
import com.pmease.commons.lang.tokenizers.clike.ObjectiveCTokenizer;

public class ObjectiveCTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new ObjectiveCTokenizer(), new String[]{"clike/clike.js"}, "test.m");
	}

}
