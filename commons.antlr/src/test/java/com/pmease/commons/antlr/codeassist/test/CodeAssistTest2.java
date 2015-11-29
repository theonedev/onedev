package com.pmease.commons.antlr.codeassist.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.pmease.commons.antlr.codeassist.CodeAssist;
import com.pmease.commons.antlr.codeassist.InputStatus;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.LexerRuleRefElementSpec;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.ParseTree;
import com.pmease.commons.antlr.codeassist.SurroundingAware;

public class CodeAssistTest2 {

	private static final String[] BRANCHS = new String[]{"master", "dev", "feature1", "feature2"};

	private CodeAssist codeAssist = new CodeAssist(CodeAssistTest2Lexer.class) {

		private static final long serialVersionUID = 1L;

		@Override
		protected List<InputSuggestion> suggest(final ParseTree parseTree, Node elementNode, String matchWith) {
			if (elementNode.getSpec() instanceof LexerRuleRefElementSpec) {
				LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) elementNode.getSpec();
				if (spec.getRuleName().equals("Value")) {
					return new SurroundingAware(codeAssist, "(", ")") {

						@Override
						protected List<InputSuggestion> match(String matchWith) {
							List<InputSuggestion> suggestions = new ArrayList<>();
							if (parseTree.getLastNode().getToken().getType() == CodeAssistTest2Lexer.BRANCH) {
								for (String value: BRANCHS) {
									if (value.toLowerCase().contains(matchWith.toLowerCase()))
										suggestions.add(new InputSuggestion(value));
								}
							}
							return suggestions;
						}
						
					}.suggest(elementNode, matchWith);
				}
			}
			return null;
		}

	};
	
	@Test
	public void test() {
		List<InputSuggestion> suggestions;
		
		suggestions = codeAssist.suggest(new InputStatus("branch(master)"), "revisionCriteria");
		assertEquals(2, suggestions.size());
		assertEquals("branch(master)..:16", suggestions.get(0).toString());
		assertEquals("branch(master)...:17", suggestions.get(1).toString());
		
		suggestions = codeAssist.suggest(new InputStatus(""), "revisionCriteria");
		assertEquals(4, suggestions.size());
		assertEquals("branch(:7", suggestions.get(0).toString());
		assertEquals("tag(:4", suggestions.get(1).toString());
		assertEquals("id(:3", suggestions.get(2).toString());
		assertEquals("^:1", suggestions.get(3).toString());
		
		suggestions = codeAssist.suggest(new InputStatus("br"), "query");
		assertEquals(1, suggestions.size());
		assertEquals("branch(:7", suggestions.get(0).toString());
		
		suggestions = codeAssist.suggest(new InputStatus("branch("), "query");
		assertEquals(4, suggestions.size());
		assertEquals("branch(master):14", suggestions.get(0).toString());
		assertEquals("branch(dev):11", suggestions.get(1).toString());
		assertEquals("branch(feature1):16", suggestions.get(2).toString());
		assertEquals("branch(feature2):16", suggestions.get(3).toString());
		
		suggestions = codeAssist.suggest(new InputStatus("branch"), "query");
		assertEquals(4, suggestions.size());
		assertEquals("branch(master):14", suggestions.get(0).toString());
		assertEquals("branch(dev):11", suggestions.get(1).toString());
		assertEquals("branch(feature1):16", suggestions.get(2).toString());
		assertEquals("branch(feature2):16", suggestions.get(3).toString());

		suggestions = codeAssist.suggest(new InputStatus("branch( fea"), "query");
		assertEquals(3, suggestions.size());
		assertEquals("branch(feature1):16", suggestions.get(0).toString());
		assertEquals("branch(feature2):16", suggestions.get(1).toString());
		assertEquals("branch(fea):11", suggestions.get(2).toString());
		
		suggestions = codeAssist.suggest(new InputStatus("tag"), "query");
		assertEquals(1, suggestions.size());
		assertEquals("tag(:4", suggestions.get(0).toString());
	}
	
}
