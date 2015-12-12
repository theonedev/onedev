package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;

public abstract class SurroundingAware {
	
	private final CodeAssist codeAssist;
	
	private final String prefix;
	
	private final String suffix;
	
	public SurroundingAware(CodeAssist codeAssist, String prefix, String suffix) {
		this.codeAssist = codeAssist;
		this.prefix = prefix;
		this.suffix = suffix;
	}
	
	private boolean matches(ElementSpec spec, String content) {
		List<Token> tokens = codeAssist.lex(content);
		int start;
		int stop;
		if (tokens.isEmpty()) {
			start = stop = 0;
		} else {
			start = tokens.get(0).getStartIndex();
			stop = tokens.get(tokens.size()-1).getStopIndex()+1;
		}
		if (start == 0 && stop == content.length())
			return spec.getMatchDistance(tokens) != -1;
		else 
			return false;
	}
	
	public List<InputSuggestion> suggest(ElementSpec spec, String matchWith) {
		if (matchWith.startsWith(prefix))
			matchWith = matchWith.substring(prefix.length());
		if (matchWith.endsWith(suffix))
			matchWith = matchWith.substring(0, matchWith.length()-suffix.length());
		matchWith = matchWith.trim();
		
		List<InputSuggestion> suggestions = match(matchWith);
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
				checkedSuggestions.add(new InputSuggestion(content, caret, suggestion.getDescription()));
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
	 * 			a list of suggestions, value containing spaces does not have to be
	 * 			surrounded with prefix/suffix as the {@link SurroundingAware} class 
	 * 			will add them if necessary 
	 */
	protected abstract List<InputSuggestion> match(String surroundlessMatchWith);
}
