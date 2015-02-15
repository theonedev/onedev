package com.pmease.commons.tokenizer;

import org.junit.Test;

public class JavascriptTokenzierTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new JavascriptTokenizer.JavaScript(), new String[]{"javascript/javascript.js"}, "testfiles/test.js");
		verify(new JavascriptTokenizer.JSON(), new String[]{"javascript/javascript.js"}, "testfiles/test.json");
		verify(new JavascriptTokenizer.JSON_LD(), new String[]{"javascript/javascript.js"}, "testfiles/test.jsonld");
		verify(new JavascriptTokenizer.TypeScript(), new String[]{"javascript/javascript.js"}, "testfiles/test.ts");
	}

}
