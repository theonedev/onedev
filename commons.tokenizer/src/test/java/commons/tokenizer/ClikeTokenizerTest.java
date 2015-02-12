package commons.tokenizer;

import org.junit.Test;

import com.pmease.commons.tokenizer.ClikeTokenizer;

public class ClikeTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void testC() {
		verify(new ClikeTokenizer.C(), new String[]{"clike/clike.js"}, "testfiles/test.c");
	}

	@Test
	public void testCpp() {
		verify(new ClikeTokenizer.Cpp(), new String[]{"clike/clike.js"}, "testfiles/test.cpp");
	}
	
	@Test
	public void testJava() {
		verify(new ClikeTokenizer.Java(), new String[]{"clike/clike.js"}, "testfiles/test.java.txt");
	}

	@Test
	public void testCSharp() {
		verify(new ClikeTokenizer.CSharp(), new String[]{"clike/clike.js"}, "testfiles/test.cs");
	}

	@Test
	public void testScala() {
		verify(new ClikeTokenizer.Scala(), new String[]{"clike/clike.js"}, "testfiles/test.scala");
	}

	@Test
	public void testObjectiveC() {
		verify(new ClikeTokenizer.ObjectiveC(), new String[]{"clike/clike.js"}, "testfiles/test.m");
	}

}
