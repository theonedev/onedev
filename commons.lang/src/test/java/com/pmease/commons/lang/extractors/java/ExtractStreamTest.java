package com.pmease.commons.lang.extractors.java;

import static org.junit.Assert.fail;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.junit.Assert;
import org.junit.Test;

import com.pmease.commons.lang.extractors.ExtractException;
import com.pmease.commons.lang.extractors.ExtractStream;
import com.pmease.commons.lang.extractors.TokenFilter;

public class ExtractStreamTest {

	@Test
	public void testBalanced() {
		String text = "public class MyClass {"
				+ "  public void sayHello() {System.out.println(\"hello\");}"
				+ "}";
		
		ExtractStream extractStream = new ExtractStream(
				new JavaLexer(new ANTLRInputStream(text)), TokenFilter.DEFAULT_CHANNEL);
		
		extractStream.nextType(JavaLexer.LBRACE);
		Assert.assertEquals("}", extractStream.nextClosed(JavaLexer.LBRACE, JavaLexer.RBRACE).getText());
	}

	@Test
	public void testNonBalanced() {
		String text = "public class MyClass {"
				+ "  public void sayHello() {System.out.println(\"hello\");}";
		
		ExtractStream extractStream = new ExtractStream(
				new JavaLexer(new ANTLRInputStream(text)), TokenFilter.DEFAULT_CHANNEL);
		extractStream.nextType(JavaLexer.LBRACE);
		try {
			extractStream.nextClosed(JavaLexer.LBRACE, JavaLexer.RBRACE);
			fail();
		} catch (ExtractException e) {
		}
	}
	
}
