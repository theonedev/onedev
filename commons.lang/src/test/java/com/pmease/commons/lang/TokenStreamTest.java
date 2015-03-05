package com.pmease.commons.lang;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.pmease.commons.lang.TokenStream;
import com.pmease.commons.lang.TokenizedLine;
import com.pmease.commons.lang.tokenizers.clike.JavaTokenizer;

public class TokenStreamTest {

	@Test
	public void testBalanced() {
		String text = "public class MyClass {"
				+ "  public void sayHello() {System.out.println(\"hello\");}"
				+ "}";
		
		List<TokenizedLine> tokenizedLines = new JavaTokenizer().tokenize(text);
		TokenStream tokenStream = new TokenStream(tokenizedLines, false);
		Assert.assertEquals("}", tokenStream.nextBalanced(tokenStream.next("{")).text());
	}

	@Test
	public void testNonBalanced() {
		String text = "public class MyClass {"
				+ "  public void sayHello() {System.out.println(\"hello\");}";
		
		List<TokenizedLine> tokenizedLines = new JavaTokenizer().tokenize(text);
		TokenStream tokenStream = new TokenStream(tokenizedLines, false);
		Assert.assertTrue(tokenStream.nextBalanced(tokenStream.next("{")).isEof());
	}
	
}
