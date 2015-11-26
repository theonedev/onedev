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

public class CodeAssistTest2 {

	private Suggester suggester;
	
	private CodeAssist codeAssist = new CodeAssist(CodeAssistTest2Lexer.class) {

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
	public void test() {
		List<InputSuggestion> suggestions;
		
		/*
		suggestions = codeAssist.suggest(new InputStatus(""), "revisionCriteria");
		assertEquals(4, suggestions.size());
		assertEquals("branch(:7", suggestions.get(0).toString());
		assertEquals("tag(:4", suggestions.get(1).toString());
		assertEquals("id(:3", suggestions.get(2).toString());
		assertEquals("^:1", suggestions.get(3).toString());
		*/
		
		suggestions = codeAssist.suggest(new InputStatus("br"), "query");
		assertEquals(1, suggestions.size());
		assertEquals("branch(:7", suggestions.get(0).toString());
	}
	
	interface Suggester {
		List<InputSuggestion> suggest(ParseTree parseTree, Node elementNode, String matchWith);
	}
}
