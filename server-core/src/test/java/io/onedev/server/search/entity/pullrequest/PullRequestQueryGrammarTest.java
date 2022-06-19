package io.onedev.server.search.entity.pullrequest;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.onedev.commons.codeassist.CodeAssist;
import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputStatus;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.commons.codeassist.parser.TerminalExpect;

public class PullRequestQueryGrammarTest {

	private CodeAssist codeAssist = new CodeAssist(PullRequestQueryLexer.class, false) {

		private static final long serialVersionUID = 1L;

		@Override
		protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
			if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
				LexerRuleRefElementSpec elementSpec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
				if (elementSpec.getRuleName().equals("Quoted")) {
					return new FenceAware(codeAssist.getGrammar(), '"', '"') {
						
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
		
		suggestions = codeAssist.suggest(new InputStatus("\"Submit Date\" is until 2018-09-01 2:30PM", 40), "query");
		assertEquals(1, suggestions.size());
		assertEquals("\"Submit Date\" is until \"2018-09-01 2:30PM\":42", suggestions.get(0).toString());

		suggestions = codeAssist.suggest(new InputStatus("open", 4), "query");
		assertEquals(3, suggestions.size());
		assertEquals("open:4", suggestions.get(0).toString());
		assertEquals("\"open\" :7", suggestions.get(1).toString());
		assertEquals("open :5", suggestions.get(2).toString());
	}
	
}
