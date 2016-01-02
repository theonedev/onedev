package com.pmease.commons.antlr.codeassist;

import javax.annotation.Nullable;

import com.pmease.commons.util.pattern.Highlight;

public class InputSuggestion extends InputStatus {
	
	private static final long serialVersionUID = 1L;

	private final boolean complete;
	
	private final String description;
	
	private final Highlight highlight;

	/**
	 * Construct the input suggestion.
	 * 
	 * @param content
	 * 			content of the suggestion
	 * @param caret
	 * 			caret of the suggestion
	 * @param complete
	 * 			whether or not the suggested content is a complete representation of 
	 * 			corresponding element spec
	 * @param description
	 * 			description of the suggestion
	 * @param highlight
	 * 			optionally indicate part of the content to be highlighted, normally this is 
	 * 			occurring position of the string to be matched
	 */
	public InputSuggestion(String content, int caret, boolean complete, @Nullable String description, 
			@Nullable Highlight highlight) {
		super(content, caret);
		this.complete = complete;
		this.description = description;
		this.highlight = highlight;
	}
	
	public InputSuggestion(String content, @Nullable String description, @Nullable Highlight highlight) {
		this(content, -1, true, description, highlight);
	}
	
	public InputSuggestion(String content, @Nullable Highlight highlight) {
		this(content, null, highlight);
	}
	
	public InputSuggestion(String content) {
		this(content, null);
	}
	
	public boolean isComplete() {
		return complete;
	}

	@Nullable
	public String getDescription() {
		return description;
	}

	@Nullable
	public Highlight getHighlight() {
		return highlight;
	}

}
