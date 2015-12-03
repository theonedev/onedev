package com.pmease.commons.antlr.codeassist.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.pmease.commons.antlr.codeassist.CodeAssist;
import com.pmease.commons.antlr.codeassist.InputCompletion;
import com.pmease.commons.antlr.codeassist.InputStatus;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.ParseTree;

public class CodeAssistTest1 {

	private CodeAssist codeAssist = new CodeAssist(CodeAssistTest1Lexer.class) {

		private static final long serialVersionUID = 1L;

		@Override
		protected List<InputSuggestion> suggest(ParseTree parseTree, Node elementNode, String matchWith) {
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
	public void test()	{
		List<InputStatus> suggestions;
		
		suggestions = suggest(new InputStatus(""), "selfReference");
		assertEquals(1, suggestions.size());
		assertEquals("ab:2", suggestions.get(0).toString());
		
		suggestions = suggest(new InputStatus("ab"), "selfReference");
		assertEquals(0, suggestions.size());

		suggestions = suggest(new InputStatus("ab "), "selfReference");
		assertEquals(1, suggestions.size());
		assertEquals("ab cd:5", suggestions.get(0).toString());

		suggestions = suggest(new InputStatus(""), "mandatories");
		assertEquals(2, suggestions.size());
		assertEquals("ab c:4", suggestions.get(0).toString());
		assertEquals("cd ef g h:9", suggestions.get(1).toString());
		
		suggestions = suggest(new InputStatus("cd "), "mandatories");
		assertEquals(1, suggestions.size());
		assertEquals("cd ef g h:9", suggestions.get(0).toString());
	}

}
