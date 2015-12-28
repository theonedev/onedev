package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.Token;

import com.pmease.commons.antlr.grammar.ElementSpec;
import com.pmease.commons.antlr.grammar.Grammar;

public abstract class SurroundingAware {
	
	private final Grammar grammar;
	
	private final String prefix;
	
	private final String suffix;
	
	public SurroundingAware(Grammar grammar, String prefix, String suffix) {
		this.grammar = grammar;
		this.prefix = prefix;
		this.suffix = suffix;
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
		if (matchWith.endsWith(suffix))
			return new ArrayList<>();
		
		if (matchWith.startsWith(prefix))
			matchWith = matchWith.substring(prefix.length());
		matchWith = matchWith.trim();
		
		List<InputSuggestion> suggestions = match(matchWith);
		if (suggestions != null) {
			List<InputSuggestion> checkedSuggestions = new ArrayList<>();
			
			boolean matchWithIncluded = false;
			for (InputSuggestion suggestion: suggestions) {
				String content = suggestion.getContent();
				int caret = suggestion.getCaret();
				if (!matches(spec, content)) {
					content = prefix + content + suffix;
					if (caret == suggestion.getContent().length())
						caret = content.length();
					else
						caret += prefix.length();
					checkedSuggestions.add(new InputSuggestion(content, caret, true, suggestion.getDescription()));
				} else {
					checkedSuggestions.add(suggestion);
				}
				if (suggestion.getContent().equals(matchWith))
					matchWithIncluded = true;
			}
			
			/*
			 * if matchWith does not appear in suggestion list, we check to see if it should be surrounded. 
			 * For instance, you may have a rule requiring that value containing spaces should be quoted, 
			 * in this case, below code will suggest you to quote the value if it contains spaces as 
			 * otherwise it will fail the match below
			 */
			if (!matchWithIncluded && !matches(spec, matchWith)) {
				matchWith = prefix + matchWith + suffix;
				if (matches(spec, matchWith))
					checkedSuggestions.add(new InputSuggestion(matchWith, getSurroundingDescription()));
			}
			return checkedSuggestions;
		} else {
			return null;
		}
	}
	
	protected String getSurroundingDescription() {
		return null;
	}
	
	/**
	 * Match with provided string to give a list of suggestions
	 * 
	 * @param surroundlessMatchWith
	 * 			string with surrounding literals removed 
	 * @return
	 * 			a list of suggestions. If you do not have any suggestions and want code assist to 
	 * 			drill down the element to provide default suggestions, return a <tt>null</tt> value 
	 * 			instead of an empty list
	 */
	@Nullable
	protected abstract List<InputSuggestion> match(String surroundlessMatchWith);
}
