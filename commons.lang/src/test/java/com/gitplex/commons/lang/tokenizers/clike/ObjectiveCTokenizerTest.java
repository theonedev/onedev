package com.gitplex.commons.lang.tokenizers.clike;

import org.junit.Test;

import com.gitplex.commons.lang.tokenizers.clike.ObjectiveCTokenizer;
import com.gitplex.commons.lang.tokenizers.AbstractTokenizerTest;

public class ObjectiveCTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new ObjectiveCTokenizer(), new String[]{"clike/clike.js"}, "test.m");
	}

}
