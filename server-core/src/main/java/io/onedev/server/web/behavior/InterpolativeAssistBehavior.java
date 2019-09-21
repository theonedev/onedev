package io.onedev.server.web.behavior;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.Token;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.commons.codeassist.parser.ParseExpect;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.server.util.interpolative.Interpolative;
import io.onedev.server.util.interpolative.InterpolativeLexer;
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
			String mark = String.valueOf(Interpolative.MARK);
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();

			Set<String> matches = new HashSet<>();
			for (Token token: terminalExpect.getRoot().getState().getMatchedTokens()) {
				if (token.getType() == InterpolativeLexer.Variable)
					matches.add(Interpolative.unescape(token.getText()));
			}

			String unmatched = terminalExpect.getUnmatchedText();
			if (spec.getRuleName().equals("Variable") && unmatched.startsWith(mark)) {
				return new FenceAware(codeAssist.getGrammar(), mark, mark) {

					@Override
					protected List<InputSuggestion> match(String unfencedMatchWith) {
						List<InputSuggestion> suggestions = suggestVariables(unfencedMatchWith)
								.stream()
								.filter(it->!matches.contains(it.getContent()))
								.collect(Collectors.toList());
						if (unfencedMatchWith.length() != 0 
								&& !matches.contains(unfencedMatchWith) 
								&& suggestions.isEmpty()) {
							return null;
						} else {
							return suggestions.stream().map(it->it.escape(mark)).collect(Collectors.toList());
						}
					}

					@Override
					protected String getFencingDescription() {
						return null;
					}
					
				}.suggest(terminalExpect);
			} else if (spec.getRuleName().equals("Literal")) {
				List<InputSuggestion> suggestions = suggestLiterals(unmatched)
						.stream()
						.filter(it->!matches.contains(it.getContent()))
						.collect(Collectors.toList());
				if (unmatched.length() != 0 
						&& !matches.contains(unmatched) 
						&& suggestions.isEmpty()) {
					return null;
				} else {
					return suggestions.stream().map(it->it.escape(mark)).collect(Collectors.toList());
				}
			}
		}
		return null;
	}
	
	@Override
	protected List<String> getHints(TerminalExpect terminalExpect) {
		return Lists.newArrayList(
				String.format("Type '%c' to start inserting variable", Interpolative.MARK),
				String.format("Type '\\%c' to input normal '%c' character", 
						Interpolative.MARK, Interpolative.MARK));
	}
	
	@Override
	protected Optional<String> describe(ParseExpect parseExpect, String suggestedLiteral) {
		if (suggestedLiteral.equals(String.valueOf(Interpolative.MARK))) 
			return null;
		else
			return Optional.fromNullable(null);
	}

	protected abstract List<InputSuggestion> suggestVariables(String matchWith);

	protected abstract List<InputSuggestion> suggestLiterals(String matchWith);
	
}
