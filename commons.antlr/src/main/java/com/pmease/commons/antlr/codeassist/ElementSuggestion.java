package com.pmease.commons.antlr.codeassist;

import java.util.List;

public class ElementSuggestion {
	
	private final ParentedElement expectingElement;
	
	private final String matchWith;
	
	private final List<InputSuggestion> inputSuggestions;
	
	public ElementSuggestion(ParentedElement expectingElement, 
			String matchWith, List<InputSuggestion> inputSuggestions) {
		this.expectingElement = expectingElement;
		this.matchWith = matchWith;
		this.inputSuggestions = inputSuggestions;
	}

	public ParentedElement getExpectingElement() {
		return expectingElement;
	}

	public String getMatchWith() {
		return matchWith;
	}

	public List<InputSuggestion> getInputSuggestions() {
		return inputSuggestions;
	}
	
}
