package com.pmease.commons.antlr.codeassist.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.pmease.commons.antlr.codeassist.CodeAssist;
import com.pmease.commons.antlr.codeassist.InputCompletion;
import com.pmease.commons.antlr.codeassist.InputStatus;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.ParseTree;

public class CodeAssistTest5 {

	private CodeAssist codeAssist = new CodeAssist(CodeAssistTest5Lexer.class, 
			new String[]{
					"com/pmease/commons/antlr/codeassist/test/CodeAssistTest5Lexer.g4", 
					"com/pmease/commons/antlr/codeassist/test/CodeAssistTest5Parser.g4"}, 
			"CodeAssistTest5Parser.tokens") {

		private static final long serialVersionUID = 1L;

		@Override
		protected List<InputSuggestion> suggest(final ParseTree parseTree, Node elementNode, String matchWith) {
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

		suggestions = suggest(new InputStatus("select value from "), "stat");
		System.out.println(suggestions);
	}
	
}
