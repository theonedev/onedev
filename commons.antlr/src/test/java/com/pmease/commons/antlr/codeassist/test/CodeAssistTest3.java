package com.pmease.commons.antlr.codeassist.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.pmease.commons.antlr.codeassist.CodeAssist;
import com.pmease.commons.antlr.codeassist.InputStatus;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.ParseTree;
import com.pmease.commons.antlr.codeassist.RuleRefElementSpec;
import com.pmease.commons.antlr.codeassist.SurroundingAware;

public class CodeAssistTest3 {

	private static final String[] AUTHORS = new String[]{"robin shen", "steve luo"};

	private CodeAssist codeAssist = new CodeAssist(CodeAssistTest3Lexer.class) {

		private static final long serialVersionUID = 1L;

		@Override
		protected List<InputSuggestion> suggest(final ParseTree parseTree, Node elementNode, String matchWith) {
			if (elementNode.getSpec() instanceof RuleRefElementSpec) {
				RuleRefElementSpec spec = (RuleRefElementSpec) elementNode.getSpec();
				if (spec.getRuleName().equals("value")) {
					return new SurroundingAware(codeAssist, "\"", "\"") {

						@Override
						protected List<InputSuggestion> match(String matchWith) {
							List<InputSuggestion> suggestions = new ArrayList<>();
//							if (parseTree.getLastNode().getToken().getType() == CodeAssistTest2Lexer.BRANCH) {
								for (String value: AUTHORS) {
									if (value.toLowerCase().contains(matchWith.toLowerCase()))
										suggestions.add(new InputSuggestion(value, value));
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

		/*
		suggestions = codeAssist.suggest(new InputStatus(""), "query");
		assertEquals(2, suggestions.size());
		assertEquals("title::6", suggestions.get(0).toString());
		assertEquals("author::7", suggestions.get(1).toString());
		*/
		
		suggestions = codeAssist.suggest(new InputStatus("title: hello world"), "query");
		System.out.println(suggestions);
	}
	
}
