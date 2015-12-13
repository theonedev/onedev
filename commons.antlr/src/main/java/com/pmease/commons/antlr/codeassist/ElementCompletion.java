package com.pmease.commons.antlr.codeassist;

public class ElementCompletion extends InputCompletion {

	private static final long serialVersionUID = 1L;
	
	private final boolean complete;
	
	private final ParentedElement expectedElement;
	
	public ElementCompletion(ParentedElement expectedElement, int replaceBegin, 
			int replaceEnd, String replaceContent, int caret, boolean complete, 
			String description) {
		super(replaceBegin, replaceEnd, replaceContent, caret, description);
		
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