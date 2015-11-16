package com.pmease.commons.antlr.grammar;

import static org.junit.Assert.*;
import org.junit.Test;

import com.pmease.commons.antlr.grammarabstraction.Alternative;
import com.pmease.commons.antlr.grammarabstraction.AnyTokenElement;
import com.pmease.commons.antlr.grammarabstraction.BlockElement;
import com.pmease.commons.antlr.grammarabstraction.Grammar;
import com.pmease.commons.antlr.grammarabstraction.LexerRuleElement;
import com.pmease.commons.antlr.grammarabstraction.LiteralElement;
import com.pmease.commons.antlr.grammarabstraction.Rule;
import com.pmease.commons.antlr.grammarabstraction.RuleElement;
import com.pmease.commons.antlr.grammarabstraction.Element.Multiplicity;

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
		assertEquals("DOC_COMMENT", lexerRuleElement.getName());
		assertEquals(4, lexerRuleElement.getType());
		assertEquals(Multiplicity.ZERO_OR_ONE, lexerRuleElement.getMultiplicity());
		
		RuleElement ruleElement = (RuleElement) alternative.getElements().get(1);
		assertEquals("grammarType", ruleElement.getName());
		assertEquals(Multiplicity.ONE, ruleElement.getMultiplicity());
		
		ruleElement = (RuleElement) alternative.getElements().get(6);
		assertEquals("modeSpec", ruleElement.getName());
		assertEquals(Multiplicity.ZERO_OR_MORE, ruleElement.getMultiplicity());
	}

	@Test
	public void testDocComment() {
		Rule rule = grammar.getRules().get("DocComment");
		assertEquals(1, rule.getAlternatives().size());
		Alternative alternative = rule.getAlternatives().get(0);
		assertEquals(3, alternative.getElements().size());
		LiteralElement literalElement = (LiteralElement) alternative.getElements().get(0);
		assertEquals(0, literalElement.getType());
		assertEquals("/**", literalElement.getLiteral());
		assertEquals(Multiplicity.ONE, literalElement.getMultiplicity());
		
		AnyTokenElement anyTokenElement = (AnyTokenElement) alternative.getElements().get(1);
		assertEquals(Multiplicity.ZERO_OR_MORE, anyTokenElement.getMultiplicity());
		
		BlockElement blockElement = (BlockElement) alternative.getElements().get(2);
		assertEquals(2, blockElement.getAltenatives().size());
		assertEquals(1, blockElement.getAltenatives().get(0).getElements().size());
		literalElement = (LiteralElement) blockElement.getAltenatives().get(0).getElements().get(0);
		assertEquals(0, literalElement.getType());
		assertEquals("*/", literalElement.getLiteral());
		assertEquals(1, blockElement.getAltenatives().get(1).getElements().size());
		LexerRuleElement lexerRuleElement = (LexerRuleElement) blockElement.getAltenatives().get(1).getElements().get(0);
		assertEquals(-1, lexerRuleElement.getType());
		assertEquals("EOF", lexerRuleElement.getName());
	}
	
	@Test
	public void testWs() {
		Rule rule = grammar.getRules().get("Ws");
		assertEquals(2, rule.getAlternatives().size());
		assertEquals(1, rule.getAlternatives().get(0).getElements().size());
		LexerRuleElement lexerRuleElement = (LexerRuleElement) rule.getAlternatives().get(0).getElements().get(0);
		assertEquals("Hws", lexerRuleElement.getName());
	}

	@Test
	public void testVws() {
		Rule rule = grammar.getRules().get("Vws");
		assertEquals(1, rule.getAlternatives().size());
		assertEquals(1, rule.getAlternatives().get(0).getElements().size());
		assertTrue(rule.getAlternatives().get(0).getElements().get(0) instanceof AnyTokenElement);
	}

	@Test
	public void testNameStartChar() {
		Rule rule = grammar.getRules().get("NameStartChar");
		assertTrue(rule.getAlternatives().get(0).getElements().get(0) instanceof AnyTokenElement);
	}
}
