package com.pmease.commons.antlr.codeassist.parse;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.pmease.commons.antlr.Grammar;
import com.pmease.commons.antlr.codeassist.test.CodeAssistTest1Lexer;
import com.pmease.commons.antlr.codeassist.test.CodeAssistTest4Lexer;
import com.pmease.commons.antlr.parser.EarleyParser;

public class EarleyParserTest {

	private Grammar grammar;
	
	private boolean matches(String ruleName, String text) {
		return new EarleyParser(grammar.getRule(ruleName), grammar.lex(text)).matches();
	}
	
	@Test
	public void test() {
		grammar = new Grammar(CodeAssistTest1Lexer.class);
		assertTrue(matches("notRealAmbiguity", "cd"));

		grammar = new Grammar(CodeAssistTest4Lexer.class);
		assertTrue(matches("expr", "(1+2)+3"));
		assertTrue(matches("expr", "1+(2*3)"));
		assertFalse(matches("expr", "(1+2)+"));
		assertFalse(matches("expr", "1(2*3)"));
		assertFalse(matches("expr", "1/2+3)"));
	}

}
