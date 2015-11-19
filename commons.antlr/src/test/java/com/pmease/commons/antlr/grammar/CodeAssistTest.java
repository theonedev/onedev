package com.pmease.commons.antlr.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.antlr.v4.runtime.Token;
import org.junit.Test;

import com.pmease.commons.antlr.ANTLRv4Lexer;
import com.pmease.commons.antlr.codeassist.AlternativeSpec;
import com.pmease.commons.antlr.codeassist.AnyTokenElementSpec;
import com.pmease.commons.antlr.codeassist.CaretAwareText;
import com.pmease.commons.antlr.codeassist.CodeAssist;
import com.pmease.commons.antlr.codeassist.ElementSpec;
import com.pmease.commons.antlr.codeassist.ElementSpec.Multiplicity;
import com.pmease.commons.antlr.codeassist.LexerRuleRefElementSpec;
import com.pmease.commons.antlr.codeassist.LiteralElementSpec;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.RuleRefElementSpec;
import com.pmease.commons.antlr.codeassist.RuleSpec;

public class CodeAssistTest {

	private CodeAssist codeAssist = new CodeAssist(ANTLRv4Lexer.class, 
			new String[]{
					"com/pmease/commons/antlr/ANTLRv4Parser.g4", 
					"com/pmease/commons/antlr/ANTLRv4Lexer.g4", 
					"com/pmease/commons/antlr/LexBasic.g4", 
					"com/pmease/commons/antlr/LexUnicode.g4"
			}, "ANTLRv4Parser.tokens") {

				@Override
				protected List<CaretAwareText> suggest(ElementSpec spec, Node parent, 
						List<Token> tokens, String matchWith) {
					return null;
				}

	};
	
	@Test
	public void testGrammarSpec() {
		RuleSpec rule = codeAssist.getRule("grammarSpec");
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
		RuleSpec rule = codeAssist.getRule("DocComment");
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
		RuleSpec rule = codeAssist.getRule("Ws");
		assertEquals(2, rule.getAlternatives().size());
		assertEquals(1, rule.getAlternatives().get(0).getElements().size());
		LexerRuleRefElementSpec lexerRuleElement = (LexerRuleRefElementSpec) rule.getAlternatives().get(0).getElements().get(0);
		assertEquals("Hws", lexerRuleElement.getRule().getName());
	}

	@Test
	public void testVws() {
		RuleSpec rule = codeAssist.getRule("Vws");
		assertEquals(1, rule.getAlternatives().size());
		assertEquals(1, rule.getAlternatives().get(0).getElements().size());
		assertTrue(rule.getAlternatives().get(0).getElements().get(0) instanceof AnyTokenElementSpec);
	}

	@Test
	public void testNameStartChar() {
		RuleSpec rule = codeAssist.getRule("NameStartChar");
		assertTrue(rule.getAlternatives().get(0).getElements().get(0) instanceof AnyTokenElementSpec);
	}
}
