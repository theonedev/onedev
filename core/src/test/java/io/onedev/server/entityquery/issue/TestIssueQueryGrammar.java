package io.onedev.server.entityquery.issue;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.onedev.codeassist.CodeAssist;
import io.onedev.codeassist.FenceAware;
import io.onedev.codeassist.InputStatus;
import io.onedev.codeassist.InputSuggestion;
import io.onedev.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.codeassist.parser.TerminalExpect;

public class TestIssueQueryGrammar {

	private CodeAssist codeAssist = new CodeAssist(IssueQueryLexer.class, false) {

		private static final long serialVersionUID = 1L;

		@Override
		protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
			if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
				LexerRuleRefElementSpec elementSpec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
				if (elementSpec.getRuleName().equals("Quoted")) {
					return new FenceAware(codeAssist.getGrammar(), "\"", "\"") {
						
						@Override
						protected List<InputSuggestion> match(String unfencedMatchWith) {
							return null;
						}
						
					}.suggest(terminalExpect);
				}
			}
			return null;
		}

	};

	@Test
	public void test()	{
		List<? extends InputStatus> suggestions;
		
		suggestions = codeAssist.suggest(new InputStatus("\"Date\" is before 2018-09-01 2:30PM", 34), "query");
		assertEquals(1, suggestions.size());
		assertEquals("\"Date\" is before \"2018-09-01 2:30PM\":36", suggestions.get(0).toString());

		suggestions = codeAssist.suggest(new InputStatus("open", 4), "query");
		assertEquals(3, suggestions.size());
		assertEquals("open:4", suggestions.get(0).toString());
		assertEquals("\"open\" :7", suggestions.get(1).toString());
		assertEquals("open :5", suggestions.get(2).toString());
	}
	
}
