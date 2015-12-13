package com.pmease.commons.antlr.codeassist.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.pmease.commons.antlr.codeassist.CodeAssist;
import com.pmease.commons.antlr.codeassist.InputCompletion;
import com.pmease.commons.antlr.codeassist.InputStatus;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.ParentedElement;
import com.pmease.commons.antlr.codeassist.RuleRefElementSpec;
import com.pmease.commons.antlr.codeassist.SurroundingAware;
import com.pmease.commons.antlr.codeassist.parse.Element;

public class CodeAssistTest3 {

	private static final String[] AUTHORS = new String[]{"robin shen", "steve luo", "justin"};

	private CodeAssist codeAssist = new CodeAssist(CodeAssistTest3Lexer.class) {

		private static final long serialVersionUID = 1L;

		@Override
		protected List<InputSuggestion> suggest(final ParentedElement element, String matchWith) {
			if (element.getSpec() instanceof RuleRefElementSpec) {
				RuleRefElementSpec spec = (RuleRefElementSpec) element.getSpec();
				if (spec.getRuleName().equals("value")) {
					return new SurroundingAware(codeAssist.getGrammar(), "\"", "\"") {

						@Override
						protected List<InputSuggestion> match(String matchWith) {
							List<InputSuggestion> suggestions = new ArrayList<>();
							Element criteriaElement = element.findParentByRule("criteria");
							if (criteriaElement.getFirstMatchedToken().getText().equals("author")) {
								for (String value: AUTHORS) {
									if (value.toLowerCase().contains(matchWith.toLowerCase()))
										suggestions.add(new InputSuggestion(value));
								}
							}
							return suggestions;
						}
						
					}.suggest(element.getSpec(), matchWith);
				}
			}
			return null;
		}

	};
	
	private List<InputStatus> suggest(InputStatus inputStatus, String ruleName) {
		List<InputStatus> suggestions = new ArrayList<>();
		for (InputCompletion completion: codeAssist.suggest(inputStatus, ruleName))
			suggestions.add(completion.complete(inputStatus));
		return suggestions;
	}

	@Test
	public void test() {
		List<InputStatus> suggestions;

		suggestions = suggest(new InputStatus("title: hello author:"), "query");
		assertEquals(3, suggestions.size());
		assertEquals("title: hello author:\"robin shen\":32", suggestions.get(0).toString());
		assertEquals("title: hello author:\"steve luo\":31", suggestions.get(1).toString());
		assertEquals("title: hello author:justin:26", suggestions.get(2).toString());

		suggestions = suggest(new InputStatus("title: hello world"), "query");
		assertEquals(1, suggestions.size());
		assertEquals("title: \"hello world\":20", suggestions.get(0).toString());
		
		suggestions = suggest(new InputStatus(""), "query");
		assertEquals(2, suggestions.size());
		assertEquals("title::6", suggestions.get(0).toString());
		assertEquals("author::7", suggestions.get(1).toString());
		
		suggestions = suggest(new InputStatus("title:"), "query");
		assertEquals(0, suggestions.size());
		
		suggestions = suggest(new InputStatus("author: dustin"), "query");
		assertEquals(0, suggestions.size());
		
		suggestions = suggest(new InputStatus("author: dustin "), "query");
		assertEquals(2, suggestions.size());
		assertEquals("author: dustin title::21", suggestions.get(0).toString());
		assertEquals("author: dustin author::22", suggestions.get(1).toString());
		
		suggestions = suggest(new InputStatus("author: \"robin shen\""), "query");
		assertEquals(2, suggestions.size());
		assertEquals("author: \"robin shen\"title::26", suggestions.get(0).toString());
		assertEquals("author: \"robin shen\"author::27", suggestions.get(1).toString());
		
		suggestions = suggest(new InputStatus("title: hello"), "query");
		assertEquals(0, suggestions.size());
		
		suggestions = suggest(new InputStatus("title: hello "), "query");
		assertEquals(2, suggestions.size());
		assertEquals("title: hello title::19", suggestions.get(0).toString());
		assertEquals("title: hello author::20", suggestions.get(1).toString());
	}
	
}
