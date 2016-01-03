package com.pmease.commons.antlr.codeassist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;

import com.pmease.commons.antlr.grammar.ElementSpec;
import com.pmease.commons.antlr.grammar.Grammar;
import com.pmease.commons.util.pattern.Highlight;

public class SurroundLiteral implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String prefix; 
	
	private final String suffix;
	
	public SurroundLiteral(String prefix, String suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}
	
	private boolean matches(Grammar grammar, ElementSpec spec, String content) {
		List<Token> tokens = grammar.lex(content);
		if (tokens.isEmpty()) {
			return content.length() == 0 && spec.isAllowEmpty();
		} else {
			int start = tokens.get(0).getStartIndex();
			int stop = tokens.get(tokens.size()-1).getStopIndex()+1;
			return start == 0 && stop == content.length() && spec.getEndOfMatch(tokens) == tokens.size();
		}
	}

	public String peel(String matchWith) {
		if (matchWith.startsWith(prefix))
			matchWith = matchWith.substring(prefix.length());
		return matchWith.trim();
	}
	
	public List<InputSuggestion> check(Grammar grammar, ElementSpec spec, 
			List<InputSuggestion> suggestions, String matchWith) {
		List<InputSuggestion> checkedSuggestions = new ArrayList<>();
		
		for (InputSuggestion suggestion: suggestions) {
			String content = suggestion.getContent();
			int caret = suggestion.getCaret();
			if (!matches(grammar, spec, content)) {
				content = prefix + content + suffix;
				Highlight highlight = suggestion.getHighlight();
				if (caret != -1) 
					caret += prefix.length();
				if (highlight != null)
					highlight = new Highlight(highlight.getFrom()+prefix.length(), highlight.getTo()+prefix.length());
				checkedSuggestions.add(new InputSuggestion(content, caret, true, suggestion.getDescription(), highlight));
			} else {
				checkedSuggestions.add(suggestion);
			}
		}
		
		/*
		 * Check to see if the matchWith should be surrounded and return as a suggestion if no other 
		 * suggestions. For instance, you may have a rule requiring that value containing spaces 
		 * should be quoted, in this case, below code will suggest you to quote the value if it 
		 * contains spaces as otherwise it will fail the match below
		 */
		if (checkedSuggestions.isEmpty() && !matches(grammar, spec, matchWith)) {
			matchWith = prefix + matchWith + suffix;
			if (matches(grammar, spec, matchWith)) {
				Highlight highlight = new Highlight(1, matchWith.length()-1);
				checkedSuggestions.add(new InputSuggestion(matchWith, null, highlight));
			}
		}
		return checkedSuggestions;
	}
	
}
