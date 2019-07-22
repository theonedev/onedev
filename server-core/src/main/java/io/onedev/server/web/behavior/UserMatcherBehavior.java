package io.onedev.server.web.behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.server.util.usermatcher.UserMatcherParser;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class UserMatcherBehavior extends ANTLRAssistBehavior {

	private static final String VALUE_OPEN = "(";
	
	private static final String VALUE_CLOSE = ")";
	
	private static final String ESCAPE_CHARS = "\\()";
	
	public UserMatcherBehavior() {
		super(UserMatcherParser.class, "userMatcher", false);
	}

	private List<InputSuggestion> escape(List<InputSuggestion> suggestions) {
		return suggestions.stream().map(it->it.escape(ESCAPE_CHARS)).collect(Collectors.toList());
	}
	
	@Override
	protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();
			if (spec.getRuleName().equals("Value")) {
				return new FenceAware(codeAssist.getGrammar(), VALUE_OPEN, VALUE_CLOSE) {

					@Override
					protected List<InputSuggestion> match(String unfencedMatchWith) {
						int tokenType = terminalExpect.getState().getLastMatchedToken().getType();
						List<InputSuggestion> suggestions = new ArrayList<>();
						switch (tokenType) {
						case UserMatcherParser.USER:
							suggestions.addAll(escape(SuggestionUtils.suggestUsers(unfencedMatchWith)));
							break;
						case UserMatcherParser.GROUP:
							suggestions.addAll(escape(SuggestionUtils.suggestGroups(unfencedMatchWith)));
							break;
						default: 
							return null;
						} 
						return suggestions;
					}

					@Override
					protected String getFencingDescription() {
						return "value needs to be enclosed in parenthesis";
					}
					
				}.suggest(terminalExpect);
			}
		} 
		return null;
	}
	
}
