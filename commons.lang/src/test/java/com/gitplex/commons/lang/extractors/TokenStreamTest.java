package com.gitplex.commons.lang.extractors;

import static org.junit.Assert.fail;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.junit.Assert;
import org.junit.Test;

import com.gitplex.commons.lang.extractors.ExtractException;
import com.gitplex.commons.lang.extractors.TokenStream;
import com.gitplex.commons.lang.extractors.TokenFilter;
import com.gitplex.commons.lang.extractors.java.JavaLexer;

public class TokenStreamTest {

	@Test
	public void testBalanced() {
		String text = "public class MyClass {"
				+ "  public void sayHello() {System.out.println(\"hello\");}"
				+ "}";
		
		TokenStream extractStream = new TokenStream(
				new JavaLexer(new ANTLRInputStream(text)), TokenFilter.DEFAULT_CHANNEL);
		
		extractStream.nextType(JavaLexer.LBRACE);
		Assert.assertEquals("}", extractStream.nextClosed(JavaLexer.LBRACE, JavaLexer.RBRACE).getText());
	}

	@Test
	public void testNonBalanced() {
		String text = "public class MyClass {"
				+ "  public void sayHello() {System.out.println(\"hello\");}";
		
		TokenStream extractStream = new TokenStream(
				new JavaLexer(new ANTLRInputStream(text)), TokenFilter.DEFAULT_CHANNEL);
		extractStream.nextType(JavaLexer.LBRACE);
		try {
			extractStream.nextClosed(JavaLexer.LBRACE, JavaLexer.RBRACE);
			fail();
		} catch (ExtractException e) {
		}
	}
	
}
