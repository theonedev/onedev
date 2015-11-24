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
import com.pmease.commons.antlr.codeassist.CodeAssist;
import com.pmease.commons.antlr.codeassist.ElementSpec;
import com.pmease.commons.antlr.codeassist.ElementSpec.Multiplicity;
import com.pmease.commons.antlr.codeassist.EofElementSpec;
import com.pmease.commons.antlr.codeassist.InputStatus;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.LexerRuleRefElementSpec;
import com.pmease.commons.antlr.codeassist.LiteralElementSpec;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.RuleRefElementSpec;
import com.pmease.commons.antlr.codeassist.RuleSpec;
import com.pmease.commons.antlr.codeassist.AssistStream;

public class CodeAssistTest {

	private Suggester suggester;
	
	private CodeAssist codeAssist = new CodeAssist(ANTLRv4Lexer.class, 
			new String[]{
					"com/pmease/commons/antlr/ANTLRv4Parser.g4", 
					"com/pmease/commons/antlr/ANTLRv4Lexer.g4", 
					"com/pmease/commons/antlr/LexBasic.g4", 
					"com/pmease/commons/antlr/LexUnicode.g4"
			}, "ANTLRv4Parser.tokens") {

				private static final long serialVersionUID = 1L;

				@Override
				protected List<InputSuggestion> suggest(ElementSpec spec, Node parent, 
						String matchWith, AssistStream stream) {
					return suggester.suggest(spec, parent, matchWith, stream);
				}

	};
	
	@Before
	public void setup() {
		suggester = new Suggester() {

			@Override
			public List<InputSuggestion> suggest(ElementSpec spec, Node parent, String matchWith, AssistStream stream) {
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
		assertTrue(blockRule.getAlternatives().get(1).getElements().get(0) instanceof EofElementSpec);
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
		List<InputSuggestion> suggestions;
		
		suggestions = codeAssist.suggest(new InputStatus(""), "grammarSpec");
		assertEquals(4, suggestions.size());
		assertEquals("/**;", suggestions.get(0).getContent());
		assertEquals("lexer grammar;", suggestions.get(1).getContent());
		assertEquals("parser grammar;", suggestions.get(2).getContent());
		assertEquals("grammar;", suggestions.get(3).getContent());
		
		suggestions = codeAssist.suggest(new InputStatus("parser"), "grammarSpec");
		assertEquals(1, suggestions.size());
		assertEquals("parser grammar", suggestions.get(0).getContent());
		
		suggestions = codeAssist.suggest(new InputStatus("gr"), "grammarSpec");
		assertEquals(1, suggestions.size());
		assertEquals("grammar;", suggestions.get(0).getContent());
		
		suggestions = codeAssist.suggest(new InputStatus("grammar", 4), "grammarSpec");
		assertEquals(1, suggestions.size());
		assertEquals("grammar:7", suggestions.get(0).toString());
		
		suggestions = codeAssist.suggest(new InputStatus("grammar grammar1;rule1:TOKEN1;"), "grammarSpec");
		assertEquals(8, suggestions.size());
		assertEquals("grammar grammar1;rule1:TOKEN1;catch[]{}:36", suggestions.get(0).toString());
		assertEquals("grammar grammar1;rule1:TOKEN1;finally{}:38", suggestions.get(1).toString());
		assertEquals("grammar grammar1;rule1:TOKEN1;/**:33", suggestions.get(2).toString());
		assertEquals("grammar grammar1;rule1:TOKEN1;public:;:36", suggestions.get(3).toString());
		assertEquals("grammar grammar1;rule1:TOKEN1;private:;:37", suggestions.get(4).toString());
		assertEquals("grammar grammar1;rule1:TOKEN1;protected:;:39", suggestions.get(5).toString());
		assertEquals("grammar grammar1;rule1:TOKEN1;fragment:38", suggestions.get(6).toString());
		assertEquals("grammar grammar1;rule1:TOKEN1;mode;:34", suggestions.get(7).toString());
	}
	
	@Test
	public void testSuggestOptionsSpec() {
		List<InputSuggestion> suggestions;

		suggestions = codeAssist.suggest(new InputStatus(""), "optionsSpec");
		assertEquals(1, suggestions.size());
		assertEquals("options{}:8", suggestions.get(0).toString());

		suggestions = codeAssist.suggest(new InputStatus("options{100}", 2), "optionsSpec");
		assertEquals(1, suggestions.size());
		assertEquals("options{100}:8", suggestions.get(0).toString());
		
		suggestions = codeAssist.suggest(new InputStatus("options{a"), "optionsSpec");
		assertEquals(1, suggestions.size());
		assertEquals("options{a=:10", suggestions.get(0).toString());
		
		suggestions = codeAssist.suggest(new InputStatus("options{"), "optionsSpec");
		assertEquals(1, suggestions.size());
		assertEquals("options{}:9", suggestions.get(0).toString());
		
		suggester = new Suggester() {

			@Override
			public List<InputSuggestion> suggest(ElementSpec spec, Node parent, String matchWith, AssistStream stream) {
				if (spec instanceof RuleRefElementSpec) {
					RuleRefElementSpec ruleRefElementSpec = (RuleRefElementSpec) spec;
					if (ruleRefElementSpec.getRuleName().equals("id")) {
						List<InputSuggestion> suggestions = new ArrayList<>();
						if ("hello".startsWith(matchWith))
							suggestions.add(new InputSuggestion("hello", "hello"));
						if ("world".startsWith(matchWith))
							suggestions.add(new InputSuggestion("world", "world"));
						return suggestions;
					}
				}
				return null;
			}
			
		};
		
		suggestions = codeAssist.suggest(new InputStatus("options{he"), "optionsSpec");
		assertEquals(2, suggestions.size());
		assertEquals("options{he=:11", suggestions.get(0).toString());
		assertEquals("options{hello=;:14", suggestions.get(1).toString());
		
		suggestions = codeAssist.suggest(new InputStatus("options{"), "optionsSpec");
		assertEquals(3, suggestions.size());
		assertEquals("options{hello=;:14", suggestions.get(0).toString());
		assertEquals("options{world=;:14", suggestions.get(1).toString());
		assertEquals("options{}:9", suggestions.get(2).toString());
		
		suggestions = codeAssist.suggest(new InputStatus("options{hello=100}", 8), "optionsSpec");
		assertEquals(3, suggestions.size());
		assertEquals("options{hello=100}:14", suggestions.get(0).toString());
		assertEquals("options{world=100}:14", suggestions.get(1).toString());
		assertEquals("options{}hello=100}:9", suggestions.get(2).toString());
	}
	
	@Test
	public void testSuggestRuleSpec() {
		List<InputSuggestion> suggestions;

		suggestions = codeAssist.suggest(new InputStatus("query"), "ruleSpec");
		assertEquals(7, suggestions.size());
		assertEquals("query[]:6", suggestions.get(0).toString());
		assertEquals("query returns[]:14", suggestions.get(1).toString());
		assertEquals("query throws:12", suggestions.get(2).toString());
		assertEquals("query locals[]:13", suggestions.get(3).toString());
		assertEquals("query options{}:14", suggestions.get(4).toString());
		assertEquals("query@{}:6", suggestions.get(5).toString());
		assertEquals("query::6", suggestions.get(6).toString());
	}
	
	interface Suggester {
		List<InputSuggestion> suggest(ElementSpec spec, Node parent, 
				String matchWith, AssistStream stream);
	}
}
