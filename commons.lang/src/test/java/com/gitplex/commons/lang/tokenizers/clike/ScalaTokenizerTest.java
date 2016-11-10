package com.gitplex.commons.lang.tokenizers.clike;

import org.junit.Test;

import com.gitplex.commons.lang.tokenizers.clike.ScalaTokenizer;
import com.gitplex.commons.lang.tokenizers.AbstractTokenizerTest;

public class ScalaTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new ScalaTokenizer(), new String[]{"clike/clike.js"}, "test.scala");
	}

}
