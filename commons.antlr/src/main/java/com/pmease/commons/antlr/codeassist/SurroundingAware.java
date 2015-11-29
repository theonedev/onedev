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
	
	private boolean matches(Spec spec, String content) {
		AssistStream stream = codeAssist.lex(content);
		if (stream.isEof()) {
			return content.length() == 0 && spec.matches(stream);
		} else {
			int start = stream.getToken(0).getStartIndex();
			int stop = stream.getToken(stream.size()-2).getStopIndex()+1;
			return start == 0 && stop == content.length() && spec.matches(stream);
		}
	}
	
	public List<InputSuggestion> suggest(Node elementNode, String matchWith) {
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
			if (!matches(elementNode.getSpec(), content)) {
				content = prefix + content + suffix;
				if (caret == suggestion.getContent().length())
					caret = content.length();
				else
					caret += prefix.length();
				String label = suggestion.getLabel();
				if (label.equals(suggestion.getContent()))
					label = content;
				checkedSuggestions.add(new InputSuggestion(content, caret, 
						label, suggestion.getDescription()));
			} else {
				checkedSuggestions.add(suggestion);
			}
			if (suggestion.getContent().equals(matchWith))
				matchWithIncluded = true;
		}
		if (!matchWithIncluded && !matches(elementNode.getSpec(), matchWith)) {
			matchWith = prefix + matchWith + suffix;
			if (matches(elementNode.getSpec(), matchWith))
				checkedSuggestions.add(new InputSuggestion(matchWith));
		}
		return checkedSuggestions;
	}
	
	protected abstract List<InputSuggestion> match(String surroundlessMatchWith);
}
