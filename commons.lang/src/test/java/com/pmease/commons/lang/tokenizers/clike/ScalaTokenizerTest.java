package com.pmease.commons.lang.tokenizers.clike;

import org.junit.Test;

import com.pmease.commons.lang.tokenizers.AbstractTokenizerTest;
import com.pmease.commons.lang.tokenizers.clike.ScalaTokenizer;

public class ScalaTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new ScalaTokenizer(), new String[]{"clike/clike.js"}, "test.scala");
	}

}
