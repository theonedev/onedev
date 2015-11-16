package com.pmease.commons.antlr.grammar;

import static org.junit.Assert.*;
import org.junit.Test;

import com.pmease.commons.antlr.grammar.Element.Multiplicity;

public class GrammarTest {

	private Grammar grammar = new Grammar(
			new String[]{
					"com/pmease/commons/antlr/ANTLRv4Parser.g4", 
					"com/pmease/commons/antlr/ANTLRv4Lexer.g4", 
					"com/pmease/commons/antlr/LexBasic.g4", 
					"com/pmease/commons/antlr/LexUnicode.g4"
			}, "ANTLRv4Parser.tokens");
	
	@Test
	public void testGrammarSpec() {
		Rule rule = grammar.getRules().get("grammarSpec");
		assertEquals(1, rule.getAlternatives().size());
		Alternative alternative = rule.getAlternatives().get(0);
		assertEquals(8, alternative.getElements().size());
		LexerRuleElement lexerRuleElement = (LexerRuleElement) alternative.getElements().get(0);
		assertEquals("DOC_COMMENT", lexerRuleElement.getRuleName());
		assertEquals(4, lexerRuleElement.getTokenType());
		assertEquals(Multiplicity.ZERO_OR_ONE, lexerRuleElement.getMultiplicity());
		
		RuleElement ruleElement = (RuleElement) alternative.getElements().get(1);
		assertEquals("grammarType", ruleElement.getRuleName());
		assertEquals(Multiplicity.ONE, ruleElement.getMultiplicity());
		
		ruleElement = (RuleElement) alternative.getElements().get(6);
		assertEquals("modeSpec", ruleElement.getRuleName());
		assertEquals(Multiplicity.ZERO_OR_MORE, ruleElement.getMultiplicity());
	}

	@Test
	public void testDocComment() {
		Rule rule = grammar.getRules().get("DocComment");
		assertEquals(1, rule.getAlternatives().size());
		Alternative alternative = rule.getAlternatives().get(0);
		assertEquals(3, alternative.getElements().size());
		LiteralElement literalElement = (LiteralElement) alternative.getElements().get(0);
		assertEquals(0, literalElement.getTokenType());
		assertEquals("/**", literalElement.getLiteral());
		assertEquals(Multiplicity.ONE, literalElement.getMultiplicity());
		
		AnyTokenElement anyTokenElement = (AnyTokenElement) alternative.getElements().get(1);
		assertEquals(Multiplicity.ZERO_OR_MORE, anyTokenElement.getMultiplicity());
		
		BlockElement blockElement = (BlockElement) alternative.getElements().get(2);
		assertEquals(2, blockElement.getAltenatives().size());
		assertEquals(1, blockElement.getAltenatives().get(0).getElements().size());
		literalElement = (LiteralElement) blockElement.getAltenatives().get(0).getElements().get(0);
		assertEquals(0, literalElement.getTokenType());
		assertEquals("*/", literalElement.getLiteral());
		assertEquals(1, blockElement.getAltenatives().get(1).getElements().size());
		LexerRuleElement lexerRuleElement = (LexerRuleElement) blockElement.getAltenatives().get(1).getElements().get(0);
		assertEquals(-1, lexerRuleElement.getTokenType());
		assertEquals("EOF", lexerRuleElement.getRuleName());
	}
	
}
