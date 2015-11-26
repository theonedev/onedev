package com.pmease.commons.antlr.codeassist.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.pmease.commons.antlr.codeassist.CodeAssist;
import com.pmease.commons.antlr.codeassist.InputStatus;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.ParseTree;

public class CodeAssistTest1 {

	private Suggester suggester;
	
	private CodeAssist codeAssist = new CodeAssist(CodeAssistTest1Lexer.class) {

		private static final long serialVersionUID = 1L;

		@Override
		protected List<InputSuggestion> suggest(ParseTree parseTree, Node elementNode, String matchWith) {
			return suggester.suggest(parseTree, elementNode, matchWith);
		}

	};
	
	@Before
	public void setup() {
		suggester = new Suggester() {

			@Override
			public List<InputSuggestion> suggest(ParseTree parseTree, Node elementNode, String matchWith) {
				return null;
			}
			
		};
	}
	
	@Test
	public void test()	{
		List<InputSuggestion> suggestions;
		
		suggestions = codeAssist.suggest(new InputStatus(""), "selfReference");
		assertEquals(1, suggestions.size());
		assertEquals("ab:2", suggestions.get(0).toString());
		
		suggestions = codeAssist.suggest(new InputStatus("ab"), "selfReference");
		assertEquals(1, suggestions.size());
		assertEquals("ab cd:5", suggestions.get(0).toString());

		suggestions = codeAssist.suggest(new InputStatus(""), "mandatories");
		assertEquals(2, suggestions.size());
		assertEquals("ab c:4", suggestions.get(0).toString());
		assertEquals("cd ef g h:9", suggestions.get(1).toString());
		
		suggestions = codeAssist.suggest(new InputStatus("cd"), "mandatories");
		assertEquals(1, suggestions.size());
		assertEquals("cd ef g h:9", suggestions.get(0).toString());
	}
	
	interface Suggester {
		List<InputSuggestion> suggest(ParseTree parseTree, Node elementNode, String matchWith);
	}
}
