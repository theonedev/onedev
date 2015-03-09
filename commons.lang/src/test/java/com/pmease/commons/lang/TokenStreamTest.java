package com.pmease.commons.lang;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.pmease.commons.lang.tokenizers.clike.JavaTokenizer;

public class TokenStreamTest {

	@Test
	public void testBalanced() {
		String text = "public class MyClass {"
				+ "  public void sayHello() {System.out.println(\"hello\");}"
				+ "}";
		
		TokenStream tokenStream = new TokenStream(getTokens(text));
		
		Assert.assertEquals("}", tokenStream.nextBalanced(tokenStream.nextSymbol("{")).text());
	}

	@Test
	public void testNonBalanced() {
		String text = "public class MyClass {"
				+ "  public void sayHello() {System.out.println(\"hello\");}";
		
		TokenStream tokenStream = new TokenStream(getTokens(text));
		Assert.assertTrue(tokenStream.nextBalanced(tokenStream.nextSymbol("{")).isEof());
	}
	
	private List<LineAwareToken> getTokens(String text) {
		List<LineAwareToken> tokens = new ArrayList<>();
		int linePos = 0;
		for (TokenizedLine line: new JavaTokenizer().tokenize(text)) {
			for (Token token: line.getTokens()) {
				if (!token.isComment() && !token.isWhitespace())
					tokens.add(new LineAwareToken(token, linePos));
			}
			linePos++;
		}
		return tokens;
	}
	
}
