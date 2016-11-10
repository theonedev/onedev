package com.gitplex.commons.lang.tokenizers.clike;

import org.junit.Test;

import com.gitplex.commons.lang.tokenizers.clike.JavaTokenizer;
import com.gitplex.commons.lang.tokenizers.AbstractTokenizerTest;

public class JavaTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new JavaTokenizer(), new String[]{"clike/clike.js"}, "test.java.txt");
	}

}
