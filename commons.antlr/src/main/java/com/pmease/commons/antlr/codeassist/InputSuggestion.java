package com.pmease.commons.antlr.codeassist;

import javax.annotation.Nullable;

public class InputSuggestion extends InputStatus {
	
	private static final long serialVersionUID = 1L;

	private final String description;
	
	public InputSuggestion(String content, int caret, @Nullable String description) {
		super(content, caret);
		this.description = description;
	}
	
	public InputSuggestion(String content, @Nullable String description) {
		this(content, content.length(), description);
	}
	
	public InputSuggestion(String content) {
		this(content, null);
	}
	
	@Nullable
	public String getDescription() {
		return description;
	}

}
