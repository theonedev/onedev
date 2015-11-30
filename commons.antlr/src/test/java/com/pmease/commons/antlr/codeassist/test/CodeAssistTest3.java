package com.pmease.commons.antlr.codeassist.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.pmease.commons.antlr.codeassist.CodeAssist;
import com.pmease.commons.antlr.codeassist.InputStatus;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.ParseTree;
import com.pmease.commons.antlr.codeassist.RuleRefElementSpec;
import com.pmease.commons.antlr.codeassist.SurroundingAware;
import com.pmease.commons.antlr.codeassist.TokenNode;

public class CodeAssistTest3 {

	private static final String[] AUTHORS = new String[]{"robin shen", "steve luo", "justin"};

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
							Node criteria = parseTree.findParentNodeByRuleName(parseTree.getLastNode(), "criteria");
							TokenNode tokenNode = parseTree.getFirstTokenNode(criteria);
							if (tokenNode.getToken().getText().equals("author")) {
								for (String value: AUTHORS) {
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

		suggestions = codeAssist.suggest(new InputStatus(""), "query");
		assertEquals(2, suggestions.size());
		assertEquals("title::6", suggestions.get(0).toString());
		assertEquals("author::7", suggestions.get(1).toString());
		
		suggestions = codeAssist.suggest(new InputStatus("title:"), "query");
		assertEquals(0, suggestions.size());
		
		suggestions = codeAssist.suggest(new InputStatus("title: hello world"), "query");
		assertEquals(1, suggestions.size());
		assertEquals("title: \"hello world\":20", suggestions.get(0).toString());
		
		suggestions = codeAssist.suggest(new InputStatus("author: dustin"), "query");
		assertEquals(0, suggestions.size());
		
		suggestions = codeAssist.suggest(new InputStatus("author: dustin "), "query");
		assertEquals(2, suggestions.size());
		assertEquals("author: dustin title::21", suggestions.get(0).toString());
		assertEquals("author: dustin author::22", suggestions.get(1).toString());
		
		suggestions = codeAssist.suggest(new InputStatus("author: \"robin shen\""), "query");
		assertEquals(2, suggestions.size());
		assertEquals("author: \"robin shen\"title::26", suggestions.get(0).toString());
		assertEquals("author: \"robin shen\"author::27", suggestions.get(1).toString());
		
		suggestions = codeAssist.suggest(new InputStatus("title: hello"), "query");
		assertEquals(0, suggestions.size());
		
		suggestions = codeAssist.suggest(new InputStatus("title: hello "), "query");
		assertEquals(2, suggestions.size());
		assertEquals("title: hello title::19", suggestions.get(0).toString());
		assertEquals("title: hello author::20", suggestions.get(1).toString());
		
		suggestions = codeAssist.suggest(new InputStatus("title: hello author:"), "query");
		assertEquals(3, suggestions.size());
		assertEquals("title: hello author:\"robin shen\":32", suggestions.get(0).toString());
		assertEquals("title: hello author:\"steve luo\":31", suggestions.get(1).toString());
		assertEquals("title: hello author:justin:26", suggestions.get(2).toString());
	}
	
}
