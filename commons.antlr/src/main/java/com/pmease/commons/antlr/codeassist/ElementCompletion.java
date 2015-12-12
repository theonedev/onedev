package com.pmease.commons.antlr.codeassist;

import com.pmease.commons.antlr.codeassist.parse.ParentedElement;

public class ElementCompletion extends InputCompletion {

	private static final long serialVersionUID = 1L;
	
	private final ParentedElement expectingElement;
	
	public ElementCompletion(ParentedElement expectingElement, int replaceBegin, 
			int replaceEnd, String replaceContent, int caret, String description) {
		super(replaceBegin, replaceEnd, replaceContent, caret, description);
		
		this.expectingElement = expectingElement;
	}

	public ParentedElement getExpectingElement() {
		return expectingElement;
	}

}