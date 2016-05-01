package com.pmease.commons.antlr.codeassist;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.pmease.commons.antlr.codeassist.CodeAssist;
import com.pmease.commons.antlr.codeassist.FenceAware;
import com.pmease.commons.antlr.codeassist.InputCompletion;
import com.pmease.commons.antlr.codeassist.InputStatus;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.ParentedElement;
import com.pmease.commons.antlr.codeassist.test.CodeAssistTest2Lexer;
import com.pmease.commons.antlr.grammar.LexerRuleRefElementSpec;

public class TestCodeAssist2 {

	private static final String[] BRANCHS = new String[]{"master", "dev", "feature1", "feature2"};

	private CodeAssist codeAssist = new CodeAssist(CodeAssistTest2Lexer.class) {

		private static final long serialVersionUID = 1L;

		@Override
		protected List<InputSuggestion> suggest(ParentedElement element, String matchWith, int count) {
			if (element.getSpec() instanceof LexerRuleRefElementSpec) {
				LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) element.getSpec();
				if (spec.getRuleName().equals("Value")) {
					return new FenceAware(codeAssist.getGrammar(), "(", ")") {

						@Override
						protected List<InputSuggestion> match(String matchWith, int count) {
							if (element.getRoot().getLastMatchedToken().getType() == CodeAssistTest2Lexer.BRANCH) {
								List<InputSuggestion> suggestions = new ArrayList<>();
								for (String value: BRANCHS) {
									if (value.toLowerCase().contains(matchWith.toLowerCase()))
										suggestions.add(new InputSuggestion(value));
								}
								return suggestions;
							} else {
								return null;
							}
						}
						
					}.suggest(element.getSpec(), matchWith, count);
				}
			}
			return null;
		}

	};
	
	private List<InputStatus> suggest(InputStatus inputStatus, String ruleName) {
		List<InputStatus> suggestions = new ArrayList<>();
		for (InputCompletion completion: codeAssist.suggest(inputStatus, ruleName, Integer.MAX_VALUE))
			suggestions.add(completion.complete(inputStatus));
		return suggestions;
	}
	
	@Test
	public void test() {
		List<InputStatus> suggestions;

		suggestions = suggest(new InputStatus("message"), "query");
		assertEquals(1, suggestions.size());
		assertEquals("message(:8", suggestions.get(0).toString());
		
		suggestions = suggest(new InputStatus("branch"), "query");
		assertEquals(4, suggestions.size());
		assertEquals("branch(master):14", suggestions.get(0).toString());
		assertEquals("branch(dev):11", suggestions.get(1).toString());
		assertEquals("branch(feature1):16", suggestions.get(2).toString());
		assertEquals("branch(feature2):16", suggestions.get(3).toString());
		
		suggestions = suggest(new InputStatus("branch(feature)"), "query");
		assertEquals(12, suggestions.size());
		assertEquals("branch(feature)branch(:22", suggestions.get(0).toString());
		assertEquals("branch(feature)tag(:19", suggestions.get(1).toString());
		assertEquals("branch(feature)id(:18", suggestions.get(2).toString());
		assertEquals("branch(feature)^:16", suggestions.get(3).toString());
		assertEquals("branch(feature)before(:22", suggestions.get(4).toString());
		assertEquals("branch(feature)after(:21", suggestions.get(5).toString());
		assertEquals("branch(feature)committer(:25", suggestions.get(6).toString());
		assertEquals("branch(feature)author(:22", suggestions.get(7).toString());
		assertEquals("branch(feature)path(:20", suggestions.get(8).toString());
		assertEquals("branch(feature)message(:23", suggestions.get(9).toString());
		assertEquals("branch(feature)..:17", suggestions.get(10).toString());
		assertEquals("branch(feature)...:18", suggestions.get(11).toString());

		suggestions = suggest(new InputStatus(""), "revisionCriteria");
		assertEquals(4, suggestions.size());
		assertEquals("^:1", suggestions.get(0).toString());
		assertEquals("branch(:7", suggestions.get(1).toString());
		assertEquals("tag(:4", suggestions.get(2).toString());
		assertEquals("id(:3", suggestions.get(3).toString());
		
		suggestions = suggest(new InputStatus("branch(master)"), "revisionCriteria");
		assertEquals(2, suggestions.size());
		assertEquals("branch(master)..:16", suggestions.get(0).toString());
		assertEquals("branch(master)...:17", suggestions.get(1).toString());
		
		suggestions = suggest(new InputStatus("br"), "query");
		assertEquals(1, suggestions.size());
		assertEquals("branch(:7", suggestions.get(0).toString());

		suggestions = suggest(new InputStatus("branch("), "query");
		assertEquals(4, suggestions.size());
		assertEquals("branch(master):14", suggestions.get(0).toString());
		assertEquals("branch(dev):11", suggestions.get(1).toString());
		assertEquals("branch(feature1):16", suggestions.get(2).toString());
		assertEquals("branch(feature2):16", suggestions.get(3).toString());
		
		suggestions = suggest(new InputStatus("branch( fea"), "query");
		assertEquals(2, suggestions.size());
		assertEquals("branch(feature1):16", suggestions.get(0).toString());
		assertEquals("branch(feature2):16", suggestions.get(1).toString());
		
		suggestions = suggest(new InputStatus("tag"), "query");
		assertEquals(1, suggestions.size());
		assertEquals("tag(:4", suggestions.get(0).toString());
		
		suggestions = suggest(new InputStatus("branch(master)t"), "query");
		assertEquals(1, suggestions.size());
		assertEquals("branch(master)tag(:18", suggestions.get(0).toString());
	}
	
}
