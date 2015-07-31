package com.pmease.commons.lang.extractors.java;

import static org.junit.Assert.fail;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.junit.Assert;
import org.junit.Test;

import com.pmease.commons.lang.extractors.ExtractStream;
import com.pmease.commons.lang.extractors.TokenFilter;
import com.pmease.commons.lang.extractors.UnexpectedTokenException;
import com.pmease.commons.lang.java.JavaLexer;

public class ExtractStreamTest {

	@Test
	public void testBalanced() {
		String text = "public class MyClass {"
				+ "  public void sayHello() {System.out.println(\"hello\");}"
				+ "}";
		
		ExtractStream tokenStream = new ExtractStream(
				new JavaLexer(new ANTLRInputStream(text)), TokenFilter.DEFAULT_CHANNEL);
		
		tokenStream.nextType(JavaLexer.LBRACE);
		Assert.assertEquals("}", tokenStream.nextClosed(JavaLexer.LBRACE, JavaLexer.RBRACE).getText());
	}

	@Test
	public void testNonBalanced() {
		String text = "public class MyClass {"
				+ "  public void sayHello() {System.out.println(\"hello\");}";
		
		ExtractStream tokenStream = new ExtractStream(
				new JavaLexer(new ANTLRInputStream(text)), TokenFilter.DEFAULT_CHANNEL);
		tokenStream.nextType(JavaLexer.LBRACE);
		try {
			tokenStream.nextClosed(JavaLexer.LBRACE, JavaLexer.RBRACE);
			fail();
		} catch (UnexpectedTokenException e) {
		}
	}
	
}
