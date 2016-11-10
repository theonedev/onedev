package com.gitplex.commons.lang.tokenizers.clike;

import org.junit.Test;

import com.gitplex.commons.lang.tokenizers.clike.CSharpTokenizer;
import com.gitplex.commons.lang.tokenizers.AbstractTokenizerTest;

public class CSharpTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new CSharpTokenizer(), new String[]{"clike/clike.js"}, "test.cs");
	}

}
