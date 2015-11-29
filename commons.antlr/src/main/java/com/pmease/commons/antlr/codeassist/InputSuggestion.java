package com.pmease.commons.antlr.codeassist;

import javax.annotation.Nullable;

public class InputSuggestion extends InputStatus {
	
	private static final long serialVersionUID = 1L;

	private final String label;
	
	private final String description;
	
	public InputSuggestion(String content, int caret, String label, @Nullable String description) {
		super(content, caret);
		this.label = label;
		this.description = description;
	}
	
	public InputSuggestion(String content, String label, @Nullable String description) {
		this(content, content.length(), label, description);
	}
	
	public InputSuggestion(String content, @Nullable String description) {
		this(content, content, description);
	}
	
	public InputSuggestion(String content) {
		this(content, null);
	}
	
	public String getLabel() {
		return label;
	}

	@Nullable
	public String getDescription() {
		return description;
	}

}
