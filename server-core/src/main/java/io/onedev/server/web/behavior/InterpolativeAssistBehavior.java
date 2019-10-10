package io.onedev.server.web.behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Optional;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.commons.codeassist.parser.ParseExpect;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.interpolative.InterpolativeParser;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;

@SuppressWarnings("serial")
public abstract class InterpolativeAssistBehavior extends ANTLRAssistBehavior {

	public InterpolativeAssistBehavior() {
		super(InterpolativeParser.class, "interpolative", false);
	}

	@Override
	protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();

			String unmatched = terminalExpect.getUnmatchedText();
			if (spec.getRuleName().equals("Variable") && unmatched.startsWith("@")) {
				return new FenceAware(codeAssist.getGrammar(), '@', '@') {

					@Override
					protected List<InputSuggestion> match(String matchWith) {
						return suggestVariables(matchWith);
					}

					@Override
					protected String getFencingDescription() {
						return null;
					}
					
				}.suggest(terminalExpect);
			} else if (spec.getRuleName().equals("Literal")) {
				unmatched = StringUtils.unescape(unmatched);
				List<InputSuggestion> suggestions = suggestLiterals(unmatched);
				if (!suggestions.isEmpty()) 
					return suggestions.stream().map(it->it.escape("@")).collect(Collectors.toList());
				else if (unmatched.length() == 0) 
					return new ArrayList<>();
				else 
					return null;
			}
		}
		return null;
	}
	
	@Override
	protected Optional<String> describe(ParseExpect parseExpect, String suggestedLiteral) {
		if (suggestedLiteral.equals("@")) 
			return null;
		else
			return Optional.fromNullable(null);
	}

	protected abstract List<InputSuggestion> suggestVariables(String matchWith);

	protected abstract List<InputSuggestion> suggestLiterals(String matchWith);
	
}
