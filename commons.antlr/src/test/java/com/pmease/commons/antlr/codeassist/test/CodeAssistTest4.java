package com.pmease.commons.antlr.codeassist.test;

import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.pmease.commons.antlr.codeassist.CodeAssist;
import com.pmease.commons.antlr.codeassist.InputStatus;
import com.pmease.commons.antlr.codeassist.InputSuggestion;
import com.pmease.commons.antlr.codeassist.Node;
import com.pmease.commons.antlr.codeassist.ParseTree;

public class CodeAssistTest4 {

	private CodeAssist codeAssist = new CodeAssist(CodeAssistTest4Lexer.class) {

		private static final long serialVersionUID = 1L;

		@Override
		protected List<InputSuggestion> suggest(final ParseTree parseTree, Node elementNode, String matchWith) {
			return null;
		}

	};
	
	@Test
	public void test() {
		List<InputSuggestion> suggestions;

		suggestions = codeAssist.suggest(new InputStatus("a"), "stat");
		assertEquals(6, suggestions.size());
		assertEquals("a*:2", suggestions.get(0).toString());
		assertEquals("a/:2", suggestions.get(1).toString());
		assertEquals("a+:2", suggestions.get(2).toString());
		assertEquals("a-:2", suggestions.get(3).toString());
		assertEquals("a;:2", suggestions.get(4).toString());
		assertEquals("a=:2", suggestions.get(5).toString());

		suggestions = codeAssist.suggest(new InputStatus(""), "stat");
		assertEquals(2, suggestions.size());
		assertEquals("(:1", suggestions.get(0).toString());
		assertEquals(";:1", suggestions.get(1).toString());

		suggestions = codeAssist.suggest(new InputStatus("5*"), "stat");
		assertEquals(1, suggestions.size());
		assertEquals("5*(:3", suggestions.get(0).toString());

		suggestions = codeAssist.suggest(new InputStatus("5"), "stat");
		assertEquals(5, suggestions.size());
		assertEquals("5*:2", suggestions.get(0).toString());
		assertEquals("5/:2", suggestions.get(1).toString());
		assertEquals("5+:2", suggestions.get(2).toString());
		assertEquals("5-:2", suggestions.get(3).toString());
		assertEquals("5;:2", suggestions.get(4).toString());
	}
	
}
