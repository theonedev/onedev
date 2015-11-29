package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

public abstract class SurroundingAware {
	
	private final CodeAssist codeAssist;
	
	private final String prefix;
	
	private final String suffix;
	
	public SurroundingAware(CodeAssist codeAssist, String prefix, String suffix) {
		this.codeAssist = codeAssist;
		this.prefix = prefix;
		this.suffix = suffix;
	}
	
	public List<InputSuggestion> suggest(Node elementNode, String matchWith) {
		String surroundlessMatchWith = matchWith;
		if (surroundlessMatchWith.startsWith(prefix))
			surroundlessMatchWith = surroundlessMatchWith.substring(prefix.length());
		if (surroundlessMatchWith.endsWith(suffix))
			surroundlessMatchWith = surroundlessMatchWith.substring(0, surroundlessMatchWith.length()-suffix.length());
		surroundlessMatchWith = surroundlessMatchWith.trim();
		
		List<InputSuggestion> suggestions = match(surroundlessMatchWith);
		List<InputSuggestion> checkedSuggestions = new ArrayList<>();
		
		boolean matchFound = false;
		for (InputSuggestion suggestion: suggestions) {
			String content = suggestion.getContent();
			int caret = suggestion.getCaret();
			if (!elementNode.getSpec().matches(codeAssist.lex(content))) {
				content = prefix + content + suffix;
				if (caret == suggestion.getContent().length())
					caret = content.length();
				else
					caret += prefix.length();
				checkedSuggestions.add(new InputSuggestion(content, caret, suggestion.getDescription()));
			} else {
				checkedSuggestions.add(suggestion);
			}
			if (suggestion.getContent().equals(surroundlessMatchWith))
				matchFound = true;
		}
		if (!matchFound && !elementNode.getSpec().matches(codeAssist.lex(surroundlessMatchWith))) {
			String surroundedMatchWith = prefix + surroundlessMatchWith + suffix;
			if (!surroundedMatchWith.equals(matchWith) 
					&& elementNode.getSpec().matches(codeAssist.lex(surroundedMatchWith))) {
				checkedSuggestions.add(new InputSuggestion(surroundedMatchWith, 
						surroundedMatchWith.length(), surroundedMatchWith));
			}
		}
		return checkedSuggestions;
	}
	
	protected abstract List<InputSuggestion> match(String surroundlessMatchWith);
}
