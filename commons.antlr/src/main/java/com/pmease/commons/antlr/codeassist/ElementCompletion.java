package com.pmease.commons.antlr.codeassist;

public class ElementCompletion extends InputCompletion {

	private static final long serialVersionUID = 1L;
	
	private final ParentedElement expectingElement;
	
	public ElementCompletion(ParentedElement expectingElement, int replaceBegin, 
			int replaceEnd, String replaceContent, int caret, boolean complete, 
			String description) {
		super(replaceBegin, replaceEnd, replaceContent, caret, complete, description);
		
		this.expectingElement = expectingElement;
	}

	public ParentedElement getExpectingElement() {
		return expectingElement;
	}

}