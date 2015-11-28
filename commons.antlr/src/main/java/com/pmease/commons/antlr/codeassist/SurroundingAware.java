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
		if (matchWith.startsWith(prefix))
			matchWith = matchWith.substring(prefix.length());
		matchWith = matchWith.trim();
		
		List<InputSuggestion> suggestions = match(matchWith);
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
			if (suggestion.getContent().equals(matchWith))
				matchFound = true;
		}
		if (!matchFound && !elementNode.getSpec().matches(codeAssist.lex(matchWith))) {
			String surroundedMatchWith = prefix + matchWith + suffix;
			if (elementNode.getSpec().matches(codeAssist.lex(surroundedMatchWith))) {
				checkedSuggestions.add(new InputSuggestion(surroundedMatchWith, 
						surroundedMatchWith.length(), surroundedMatchWith));
			}
		}
		return checkedSuggestions;
	}
	
	protected abstract List<InputSuggestion> match(String matchWith);
}
