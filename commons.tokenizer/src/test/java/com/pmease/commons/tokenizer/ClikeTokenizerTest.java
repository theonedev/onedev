package com.pmease.commons.tokenizer;

import org.junit.Test;

import com.pmease.commons.tokenizer.ClikeTokenizer;

public class ClikeTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new ClikeTokenizer.C(), new String[]{"clike/clike.js"}, "testfiles/test.c");
		verify(new ClikeTokenizer.Cpp(), new String[]{"clike/clike.js"}, "testfiles/test.cpp");
		verify(new ClikeTokenizer.Java(), new String[]{"clike/clike.js"}, "testfiles/test.java.txt");
		verify(new ClikeTokenizer.CSharp(), new String[]{"clike/clike.js"}, "testfiles/test.cs");
		verify(new ClikeTokenizer.Scala(), new String[]{"clike/clike.js"}, "testfiles/test.scala");
		verify(new ClikeTokenizer.ObjectiveC(), new String[]{"clike/clike.js"}, "testfiles/test.m");
	}

}
