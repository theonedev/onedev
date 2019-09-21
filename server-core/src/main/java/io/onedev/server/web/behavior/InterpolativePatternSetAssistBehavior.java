package io.onedev.server.web.behavior;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.Token;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Optional;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.commons.codeassist.parser.ParseExpect;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.LinearRange;
import io.onedev.server.util.interpolative.Interpolative;
import io.onedev.server.util.patternset.InterpolativePatternSetLexer;
import io.onedev.server.util.patternset.InterpolativePatternSetParser;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;

@SuppressWarnings("serial")
public abstract class InterpolativePatternSetAssistBehavior extends ANTLRAssistBehavior {

	public InterpolativePatternSetAssistBehavior() {
		super(InterpolativePatternSetParser.class, "interpolative", false);
	}

	@Override
	protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();

			String mark = String.valueOf(Interpolative.MARK);
			Set<String> matches = new HashSet<>();
			for (Token token: terminalExpect.getRoot().getState().getMatchedTokens()) {
				if (token.getType() == InterpolativePatternSetLexer.Variable) {
					matches.add(Interpolative.unescape(token.getText()));
				} else if (token.getType() == InterpolativePatternSetLexer.Quoted 
						|| token.getType() == InterpolativePatternSetLexer.NQuoted) {
					matches.add(PatternSet.unescape(token.getText()));
				} 
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
			} else if (spec.getRuleName().equals("NQuoted")) {
				List<InputSuggestion> suggestions = suggestPatterns(unmatched);
				return suggestions
						.stream()
						.filter(it->!matches.contains(it.getContent()))
						.map(it->{
							if (StringUtils.containsAny(it.getContent(), " \"") || it.getContent().startsWith("-")) {
								InputSuggestion suggestion = it.escape("\"" + mark);
								suggestion = new InputSuggestion("\"" + suggestion.getContent() + "\"", 
										suggestion.getCaret()!=-1? suggestion.getCaret()+1: -1,
										suggestion.getDescription(), 
										new LinearRange(suggestion.getMatch().getFrom()+1, suggestion.getMatch().getTo()+1));
								return suggestion;
							} else {
								return it;
							}
						})
						.collect(Collectors.toList());
			} else if (spec.getRuleName().equals("Quoted") && unmatched.startsWith("\"")) {
				return new FenceAware(codeAssist.getGrammar(), "\"", "\"") {

					@Override
					protected List<InputSuggestion> match(String unfencedMatchWith) {
						List<InputSuggestion> suggestions = suggestPatterns(unfencedMatchWith)
								.stream()
								.filter(it->!matches.contains(it.getContent()))
								.collect(Collectors.toList());
						if (unfencedMatchWith.length() != 0 
								&& !matches.contains(unfencedMatchWith) 
								&& suggestions.isEmpty()) {
							return null;
						} else {
							return suggestions.stream().map(it->it.escape("\"" + mark)).collect(Collectors.toList());
						}
					}

					@Override
					protected String getFencingDescription() {
						return null;
					}
					
				}.suggest(terminalExpect);
			}
		}
		return null;
	}
	
	@Override
	protected Optional<String> describe(ParseExpect parseExpect, String suggestedLiteral) {
		if (suggestedLiteral.equals(String.valueOf(Interpolative.MARK)))
			return null;
		String description;
		switch (suggestedLiteral) {
		case "-":
			description = "exclude"; 
			break;
		case " ":
			description = "space";
			break;
		case "\"":
			return null;
		default:
			description = null;
		}
		return Optional.fromNullable(description);
	}

	protected abstract List<InputSuggestion> suggestVariables(String matchWith);
	
	protected abstract List<InputSuggestion> suggestPatterns(String matchWith);

}
