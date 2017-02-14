package com.gitplex.commons.antlr.codeassist;

import javax.annotation.Nullable;

import com.gitplex.jsymbol.Range;

public class ElementCompletion extends InputCompletion {

	private static final long serialVersionUID = 1L;
	
	private final boolean complete;
	
	private final ParentedElement expectedElement;
	
	public ElementCompletion(ParentedElement expectedElement, int replaceBegin, 
			int replaceEnd, String replaceContent, int caret, boolean complete, 
			@Nullable String label, @Nullable String description, @Nullable Range highlight) {
		super(replaceBegin, replaceEnd, replaceContent, caret, label, description, highlight);
		
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