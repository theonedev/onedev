package com.pmease.commons.antlr.codeassist;

import javax.annotation.Nullable;

import com.pmease.commons.util.pattern.Highlight;

public class ElementCompletion extends InputCompletion {

	private static final long serialVersionUID = 1L;
	
	private final boolean complete;
	
	private final ParentedElement expectedElement;
	
	public ElementCompletion(ParentedElement expectedElement, int replaceBegin, 
			int replaceEnd, String replaceContent, int caret, boolean complete, 
			@Nullable String description, @Nullable Highlight highlight) {
		super(replaceBegin, replaceEnd, replaceContent, caret, description, highlight);
		
		this.complete = complete;
		this.expectedElement = expectedElement;
	}

	public ParentedElement getExpectedElement() {
		return expectedElement;
	}

	public boolean isComplete() {
		return complete;
	}

}