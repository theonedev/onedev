package com.pmease.commons.antlr.codeassist;

public class InputSuggestion extends InputStatus {
	
	private static final long serialVersionUID = 1L;
	
	private final String description;
	
	public InputSuggestion(String content, int caret, String description) {
		super(content, caret);
		this.description = description;
	}
	
	public InputSuggestion(String content, String description) {
		this(content, content.length(), description);
	}
	
	public String getDescription() {
		return description;
	}

}
