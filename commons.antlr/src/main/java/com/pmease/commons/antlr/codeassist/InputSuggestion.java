package com.pmease.commons.antlr.codeassist;

import javax.annotation.Nullable;

public class InputSuggestion extends InputStatus {
	
	private final String description;
	
	public InputSuggestion(String content, int caret, @Nullable String description) {
		super(content, caret);
		this.description = description;
	}
	
	public InputSuggestion(String content, int caret) {
		this(content, caret, null);
	}

	public InputSuggestion(String content) {
		this(content, content.length());
	}
	
	@Nullable
	public String getDescription() {
		return description;
	}

}
