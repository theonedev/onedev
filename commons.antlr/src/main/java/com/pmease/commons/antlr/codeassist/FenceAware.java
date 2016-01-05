package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.Token;

import com.pmease.commons.antlr.grammar.ElementSpec;
import com.pmease.commons.antlr.grammar.Grammar;
import com.pmease.commons.util.Range;

public abstract class FenceAware {
	
	private final Grammar grammar;
	
	private final String open;
	
	private final String close;
	
	public FenceAware(Grammar grammar, String open, String close) {
		this.grammar = grammar;
		this.open = open;
		this.close = close;
	}
	
	private boolean matches(ElementSpec spec, String content) {
		List<Token> tokens = grammar.lex(content);
		if (tokens.isEmpty()) {
			return content.length() == 0 && spec.isAllowEmpty();
		} else {
			int start = tokens.get(0).getStartIndex();
			int stop = tokens.get(tokens.size()-1).getStopIndex()+1;
			return start == 0 && stop == content.length() && spec.getEndOfMatch(tokens) == tokens.size();
		}
	}
	
	public List<InputSuggestion> suggest(ElementSpec spec, String matchWith) {
		String unfencedMatchWith = matchWith;
		if (matchWith.startsWith(open))
			unfencedMatchWith = unfencedMatchWith.substring(open.length());
		unfencedMatchWith = unfencedMatchWith.trim();
		
		List<InputSuggestion> suggestions = match(unfencedMatchWith);
		if (suggestions != null) {
			List<InputSuggestion> checkedSuggestions = new ArrayList<>();
			
			for (InputSuggestion suggestion: suggestions) {
				String content = suggestion.getContent();
				int caret = suggestion.getCaret();
				if (!matches(spec, content)) {
					content = open + content + close;
					Range matchRange = suggestion.getMatchRange();
					if (caret != -1) 
						caret += open.length();
					if (matchRange != null)
						matchRange = new Range(matchRange.getFrom()+open.length(), matchRange.getTo()+open.length());
					checkedSuggestions.add(new InputSuggestion(content, caret, true, suggestion.getDescription(), matchRange));
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
			if (checkedSuggestions.isEmpty() && matchWith.length() != 0 && !matches(spec, unfencedMatchWith)) {
				unfencedMatchWith = open + unfencedMatchWith + close;
				if (matches(spec, unfencedMatchWith)) {
					Range matchRange = new Range(1, unfencedMatchWith.length()-1);
					checkedSuggestions.add(new InputSuggestion(unfencedMatchWith, getFencingDescription(), matchRange));
				}
			}
			
			if (checkedSuggestions.isEmpty() && matchWith.length() == 0)
				checkedSuggestions.add(new InputSuggestion(open, null, null));
			return checkedSuggestions;
		} else {
			return null;
		}
	}
	
	protected String getFencingDescription() {
		return null;
	}
	
	/**
	 * Match with provided string to give a list of suggestions
	 * 
	 * @param unfencedMatchWith
	 * 			string with fencing literals removed 
	 * @return
	 * 			a list of suggestions. If you do not have any suggestions and want code assist to 
	 * 			drill down the element to provide default suggestions, return a <tt>null</tt> value 
	 * 			instead of an empty list
	 */
	@Nullable
	protected abstract List<InputSuggestion> match(String unfencedMatchWith);
}
