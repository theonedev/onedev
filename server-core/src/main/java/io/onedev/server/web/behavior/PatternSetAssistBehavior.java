package io.onedev.server.web.behavior;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.Token;

import com.google.common.base.Optional;

import io.onedev.commons.codeassist.FenceAware;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.grammar.LexerRuleRefElementSpec;
import io.onedev.commons.codeassist.parser.ParseExpect;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.patternset.PatternSetLexer;
import io.onedev.server.util.patternset.PatternSetParser;
import io.onedev.server.web.behavior.inputassist.ANTLRAssistBehavior;

@SuppressWarnings("serial")
public abstract class PatternSetAssistBehavior extends ANTLRAssistBehavior {

	public PatternSetAssistBehavior() {
		super(PatternSetParser.class, "patterns", false);
	}

	@Override
	protected List<InputSuggestion> suggest(TerminalExpect terminalExpect) {
		if (terminalExpect.getElementSpec() instanceof LexerRuleRefElementSpec) {
			LexerRuleRefElementSpec spec = (LexerRuleRefElementSpec) terminalExpect.getElementSpec();

			Set<String> matches = new HashSet<>();
			for (Token token: terminalExpect.getRoot().getState().getMatchedTokens()) {
				if (token.getType() == PatternSetLexer.Quoted)
					matches.add(StringUtils.unescape(FenceAware.unfence(token.getText())));
				else
					matches.add(StringUtils.unescape(token.getText()));
			}

			String unmatched = terminalExpect.getUnmatchedText();
			if (spec.getRuleName().equals("NQuoted")) {
				List<InputSuggestion> suggestions = suggest(unmatched);
				return suggestions
						.stream()
						.filter(it -> !matches.contains(it.getContent()) || it.getContent().endsWith("/"))
						.map(it -> {
							if (it.getContent().contains(" ") || it.getContent().startsWith("-")) {
								InputSuggestion suggestion = it.escape("\"");
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
				/*
				 *  provide this suggestion only when we typed quote as otherwise we will have duplicated suggestions
				 *  (one for nquoted, and one for quoted) 
				 */
				return new FenceAware(codeAssist.getGrammar(), '"', '"') {

					@Override
					protected List<InputSuggestion> match(String matchWith) {
						List<InputSuggestion> suggestions = PatternSetAssistBehavior.this.suggest(matchWith)
								.stream()
								.filter(it -> !matches.contains(it.getContent()) || it.getContent().endsWith("/"))
								.collect(Collectors.toList());
						if (!suggestions.isEmpty() || matches.contains(matchWith) || matchWith.length() == 0) 
							return suggestions;
						else 
							return null;
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

	protected abstract List<InputSuggestion> suggest(String matchWith);

}
