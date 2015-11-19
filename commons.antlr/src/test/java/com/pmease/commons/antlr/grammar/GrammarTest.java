package com.pmease.commons.antlr.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.pmease.commons.antlr.grammarspec.AlternativeSpec;
import com.pmease.commons.antlr.grammarspec.AnyTokenElementSpec;
import com.pmease.commons.antlr.grammarspec.ElementSpec.Multiplicity;
import com.pmease.commons.antlr.grammarspec.CodeAssist;
import com.pmease.commons.antlr.grammarspec.LexerRuleRefElementSpec;
import com.pmease.commons.antlr.grammarspec.LiteralElementSpec;
import com.pmease.commons.antlr.grammarspec.RuleRefElementSpec;
import com.pmease.commons.antlr.grammarspec.RuleSpec;

public class GrammarTest {

	private CodeAssist grammar = new CodeAssist(
			new String[]{
					"com/pmease/commons/antlr/ANTLRv4Parser.g4", 
					"com/pmease/commons/antlr/ANTLRv4Lexer.g4", 
					"com/pmease/commons/antlr/LexBasic.g4", 
					"com/pmease/commons/antlr/LexUnicode.g4"
			}, "ANTLRv4Parser.tokens");
	
	@Test
	public void testGrammarSpec() {
		RuleSpec rule = grammar.getRule("grammarSpec");
		assertEquals(1, rule.getAlternatives().size());
		AlternativeSpec alternative = rule.getAlternatives().get(0);
		assertEquals(8, alternative.getElements().size());
		LexerRuleRefElementSpec lexerRuleElement = (LexerRuleRefElementSpec) alternative.getElements().get(0);
		assertEquals("DOC_COMMENT", lexerRuleElement.getRule().getName());
		assertEquals(4, lexerRuleElement.getType());
		assertEquals(Multiplicity.ZERO_OR_ONE, lexerRuleElement.getMultiplicity());
		
		RuleRefElementSpec ruleElement = (RuleRefElementSpec) alternative.getElements().get(1);
		assertEquals("grammarType", ruleElement.getRule().getName());
		assertEquals(Multiplicity.ONE, ruleElement.getMultiplicity());
		
		ruleElement = (RuleRefElementSpec) alternative.getElements().get(6);
		assertEquals("modeSpec", ruleElement.getRule().getName());
		assertEquals(Multiplicity.ZERO_OR_MORE, ruleElement.getMultiplicity());
	}

	@Test
	public void testDocComment() {
		RuleSpec rule = grammar.getRule("DocComment");
		assertEquals(1, rule.getAlternatives().size());
		AlternativeSpec alternative = rule.getAlternatives().get(0);
		assertEquals(3, alternative.getElements().size());
		LiteralElementSpec literalElement = (LiteralElementSpec) alternative.getElements().get(0);
		assertEquals(0, literalElement.getType());
		assertEquals("/**", literalElement.getLiteral());
		assertEquals(Multiplicity.ONE, literalElement.getMultiplicity());
		
		AnyTokenElementSpec anyTokenElement = (AnyTokenElementSpec) alternative.getElements().get(1);
		assertEquals(Multiplicity.ZERO_OR_MORE, anyTokenElement.getMultiplicity());
		
		RuleRefElementSpec ruleRefElement = (RuleRefElementSpec) alternative.getElements().get(2);
		RuleSpec blockRule = ruleRefElement.getRule();
		assertEquals(2, blockRule.getAlternatives().size());
		assertEquals(1, blockRule.getAlternatives().get(0).getElements().size());
		literalElement = (LiteralElementSpec) blockRule.getAlternatives().get(0).getElements().get(0);
		assertEquals(0, literalElement.getType());
		assertEquals("*/", literalElement.getLiteral());
		assertEquals(1, blockRule.getAlternatives().get(1).getElements().size());
		LexerRuleRefElementSpec lexerRuleElement = (LexerRuleRefElementSpec) blockRule.getAlternatives().get(1).getElements().get(0);
		assertEquals(-1, lexerRuleElement.getType());
		assertEquals("EOF", lexerRuleElement.getRule().getName());
	}
	
	@Test
	public void testWs() {
		RuleSpec rule = grammar.getRule("Ws");
		assertEquals(2, rule.getAlternatives().size());
		assertEquals(1, rule.getAlternatives().get(0).getElements().size());
		LexerRuleRefElementSpec lexerRuleElement = (LexerRuleRefElementSpec) rule.getAlternatives().get(0).getElements().get(0);
		assertEquals("Hws", lexerRuleElement.getRule().getName());
	}

	@Test
	public void testVws() {
		RuleSpec rule = grammar.getRule("Vws");
		assertEquals(1, rule.getAlternatives().size());
		assertEquals(1, rule.getAlternatives().get(0).getElements().size());
		assertTrue(rule.getAlternatives().get(0).getElements().get(0) instanceof AnyTokenElementSpec);
	}

	@Test
	public void testNameStartChar() {
		RuleSpec rule = grammar.getRule("NameStartChar");
		assertTrue(rule.getAlternatives().get(0).getElements().get(0) instanceof AnyTokenElementSpec);
	}
}
