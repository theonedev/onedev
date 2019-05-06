package io.onedev.server.web.behavior;

import java.util.ArrayList;
import java.util.List;

import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputStatus;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.LinearRange;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;

@SuppressWarnings("serial")
public abstract class MultilineAssistBehavior extends InputAssistBehavior {

	@Override
	protected List<InputCompletion> getSuggestions(InputStatus inputStatus) {
		List<InputCompletion> completions = new ArrayList<>();
		String contentBeforeCaret = inputStatus.getContentBeforeCaret();
		int lineStart = contentBeforeCaret.lastIndexOf('\n') + 1;
		for (InputSuggestion suggestion: getLineSuggestions(contentBeforeCaret.substring(lineStart).trim())) {
			int caret = suggestion.getCaret();
			if (caret == -1)
				caret = suggestion.getContent().length();
			caret += lineStart;
			completions.add(new InputCompletion(
					suggestion.getContent(),
					contentBeforeCaret.substring(0, lineStart) + suggestion.getContent(), 
					caret, 
					suggestion.getDescription(), 
					suggestion.getMatch()
					));
		}
		
		return completions;
	}
	
	protected abstract List<InputSuggestion> getLineSuggestions(String matchWith);
	
	@Override
	protected List<LinearRange> getErrors(String inputContent) {
		return null;
	}
	
	@Override
	protected int getAnchor(String content) {
		int lineStart = content.lastIndexOf('\n') + 1;
		String line = content.substring(0, lineStart);
		for (int i=0; i<line.length(); i++) {
			if (!Character.isWhitespace(line.charAt(i)))
				return i + lineStart;
		}
		return content.length() + lineStart;
	}
	
}
