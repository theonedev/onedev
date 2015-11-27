package com.pmease.commons.antlr.codeassist.test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.pmease.commons.antlr.codeassist.CodeAssist;
import com.pmease.commons.antlr.codeassist.InputStatus;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.ParseTree;

public class CodeAssistTest3 {

	private Suggester suggester;
	
	private CodeAssist codeAssist = new CodeAssist(CodeAssistTest3Lexer.class) {

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
		
		suggestions = codeAssist.suggest(new InputStatus("title: hello world"), "query");
		System.out.println(suggestions);
	}
	
	interface Suggester {
		List<InputSuggestion> suggest(ParseTree parseTree, Node elementNode, String matchWith);
	}
}
