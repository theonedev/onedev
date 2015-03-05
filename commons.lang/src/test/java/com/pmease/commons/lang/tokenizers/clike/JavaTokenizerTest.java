package com.pmease.commons.lang.tokenizers.clike;

import org.junit.Test;

import com.pmease.commons.lang.tokenizers.AbstractTokenizerTest;
import com.pmease.commons.lang.tokenizers.clike.JavaTokenizer;

public class JavaTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new JavaTokenizer(), new String[]{"clike/clike.js"}, "test.java.txt");
	}

}
