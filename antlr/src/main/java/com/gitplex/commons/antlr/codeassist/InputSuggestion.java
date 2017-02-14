package com.gitplex.commons.antlr.codeassist;

import javax.annotation.Nullable;

import com.gitplex.jsymbol.Range;

public class InputSuggestion extends InputStatus {
	
	private static final long serialVersionUID = 1L;

	private final boolean complete;
	
	private final String label;
	
	private final String description;
	
	private final Range matchRange;

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
	 * @param matchRange
	 * 			optionally specifies range of the string being matched against user input
	 */
	public InputSuggestion(String content, int caret, boolean complete, @Nullable String label, 
			@Nullable String description, @Nullable Range matchRange) {
		super(content, caret);
		this.complete = complete;
		this.label = label;
		this.description = description;
		this.matchRange = matchRange;
	}
	
	public InputSuggestion(String content, int caret, boolean complete, 
			@Nullable String description, @Nullable Range matchRange) {
		this(content, caret, complete, null, description, matchRange);
	}
	
	public InputSuggestion(String content, @Nullable String description, @Nullable Range highlight) {
		this(content, -1, true, description, highlight);
	}
	
	public InputSuggestion(String content, @Nullable Range matchRange) {
		this(content, null, matchRange);
	}
	
	public InputSuggestion(String content) {
		this(content, null);
	}
	
	public boolean isComplete() {
		return complete;
	}

	public String getLabel() {
		return label;
	}

	@Nullable
	public String getDescription() {
		return description;
	}

	@Nullable
	public Range getMatchRange() {
		return matchRange;
	}

}
