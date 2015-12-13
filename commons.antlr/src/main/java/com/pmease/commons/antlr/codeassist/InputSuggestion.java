package com.pmease.commons.antlr.codeassist;

import javax.annotation.Nullable;

public class InputSuggestion extends InputStatus {
	
	private static final long serialVersionUID = 1L;

	private final boolean complete;
	
	private final String description;
	
	public InputSuggestion(String content, int caret, boolean complete, @Nullable String description) {
		super(content, caret);
		this.complete = complete;
		this.description = description;
	}
	
	public InputSuggestion(String content, @Nullable String description) {
		this(content, content.length(), true, description);
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

}
