package com.pmease.commons.antlr.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
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
import com.pmease.commons.antlr.codeassist.TokenStream;

public class CodeAssistTest {

	private Suggester suggester;
	
	private CodeAssist codeAssist = new CodeAssist(ANTLRv4Lexer.class, 
			new String[]{
					"com/pmease/commons/antlr/ANTLRv4Parser.g4", 
					"com/pmease/commons/antlr/ANTLRv4Lexer.g4", 
					"com/pmease/commons/antlr/LexBasic.g4", 
					"com/pmease/commons/antlr/LexUnicode.g4"
			}, "ANTLRv4Parser.tokens") {

				@Override
				protected List<CaretAwareText> suggest(ElementSpec spec, Node parent, 
						String matchWith, TokenStream stream) {
					return suggester.suggest(spec, parent, matchWith, stream);
				}

	};
	
	@Before
	public void setup() {
		suggester = new Suggester() {

			@Override
			public List<CaretAwareText> suggest(ElementSpec spec, Node parent, String matchWith, TokenStream stream) {
				return null;
			}
			
		};
	}
	
	@Test
	public void testParseGrammarSpec() {
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
	public void testParseDocComment() {
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
	public void testParseWs() {
		RuleSpec rule = codeAssist.getRule("Ws");
		assertEquals(2, rule.getAlternatives().size());
		assertEquals(1, rule.getAlternatives().get(0).getElements().size());
		LexerRuleRefElementSpec lexerRuleElement = (LexerRuleRefElementSpec) rule.getAlternatives().get(0).getElements().get(0);
		assertEquals("Hws", lexerRuleElement.getRule().getName());
	}

	@Test
	public void testParseVws() {
		RuleSpec rule = codeAssist.getRule("Vws");
		assertEquals(1, rule.getAlternatives().size());
		assertEquals(1, rule.getAlternatives().get(0).getElements().size());
		assertTrue(rule.getAlternatives().get(0).getElements().get(0) instanceof AnyTokenElementSpec);
	}

	@Test
	public void testParseNameStartChar() {
		RuleSpec rule = codeAssist.getRule("NameStartChar");
		assertTrue(rule.getAlternatives().get(0).getElements().get(0) instanceof AnyTokenElementSpec);
	}
	
	@Test
	public void testSuggestGrammarSpec() {
		List<CaretAwareText> suggestions;
		
		suggestions = codeAssist.suggest(new CaretAwareText(""), "grammarSpec");
		assertEquals(4, suggestions.size());
		assertEquals("/**", suggestions.get(0).getContent());
		assertEquals("lexer", suggestions.get(1).getContent());
		assertEquals("parser", suggestions.get(2).getContent());
		assertEquals("grammar", suggestions.get(3).getContent());
		
		suggestions = codeAssist.suggest(new CaretAwareText("parser"), "grammarSpec");
		assertEquals(1, suggestions.size());
		assertEquals("parser grammar", suggestions.get(0).getContent());
		
		suggestions = codeAssist.suggest(new CaretAwareText("gr"), "grammarSpec");
		assertEquals(1, suggestions.size());
		assertEquals("grammar", suggestions.get(0).getContent());

		suggestions = codeAssist.suggest(new CaretAwareText("grammar", 4), "grammarSpec");
		assertEquals(1, suggestions.size());
		assertEquals("grammar:7", suggestions.get(0).toString());
	}
	
	@Test
	public void testSuggestOptionsSpec() {
		List<CaretAwareText> suggestions;

		suggestions = codeAssist.suggest(new CaretAwareText(""), "optionsSpec");
		assertEquals(1, suggestions.size());
		assertEquals("options{}:8", suggestions.get(0).toString());
		
		suggestions = codeAssist.suggest(new CaretAwareText("options{100}", 2), "optionsSpec");
		assertEquals(1, suggestions.size());
		assertEquals("options{100}:8", suggestions.get(0).toString());
		
		suggestions = codeAssist.suggest(new CaretAwareText("options{a"), "optionsSpec");
		assertEquals(1, suggestions.size());
		assertEquals("options{a=:10", suggestions.get(0).toString());
		
		suggestions = codeAssist.suggest(new CaretAwareText("options{"), "optionsSpec");
		assertEquals(1, suggestions.size());
		assertEquals("options{}:9", suggestions.get(0).toString());
		
		suggester = new Suggester() {

			@Override
			public List<CaretAwareText> suggest(ElementSpec spec, Node parent, String matchWith, TokenStream stream) {
				if (spec instanceof RuleRefElementSpec) {
					RuleRefElementSpec ruleRefElementSpec = (RuleRefElementSpec) spec;
					if (ruleRefElementSpec.getRuleName().equals("id")) {
						List<CaretAwareText> texts = new ArrayList<>();
						if ("hello".startsWith(matchWith))
							texts.add(new CaretAwareText("hello"));
						if ("world".startsWith(matchWith))
							texts.add(new CaretAwareText("world"));
						return texts;
					}
				}
				return null;
			}
			
		};
		
		suggestions = codeAssist.suggest(new CaretAwareText("options{he"), "optionsSpec");
		assertEquals(2, suggestions.size());
		assertEquals("options{he=:11", suggestions.get(0).toString());
		assertEquals("options{hello=;:14", suggestions.get(1).toString());
		
		suggestions = codeAssist.suggest(new CaretAwareText("options{"), "optionsSpec");
		assertEquals(3, suggestions.size());
		assertEquals("options{hello=;:14", suggestions.get(0).toString());
		assertEquals("options{world=;:14", suggestions.get(1).toString());
		assertEquals("options{}:9", suggestions.get(2).toString());
		
		suggestions = codeAssist.suggest(new CaretAwareText("options{hello=100}", 8), "optionsSpec");
		assertEquals(3, suggestions.size());
		assertEquals("options{hello=100}:14", suggestions.get(0).toString());
		assertEquals("options{world=100}:14", suggestions.get(1).toString());
		assertEquals("options{}hello=100}:9", suggestions.get(2).toString());
	}
	
	interface Suggester {
		List<CaretAwareText> suggest(ElementSpec spec, Node parent, 
				String matchWith, TokenStream stream);
	}
}
