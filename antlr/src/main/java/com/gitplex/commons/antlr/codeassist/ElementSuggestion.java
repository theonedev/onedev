package com.gitplex.commons.antlr.codeassist;

import java.util.List;

public class ElementSuggestion {
	
	private final ParentedElement expectedElement;
	
	private final String matchWith;
	
	private final List<InputSuggestion> inputSuggestions;
	
	public ElementSuggestion(ParentedElement expectedElement, 
			String matchWith, List<InputSuggestion> inputSuggestions) {
		this.expectedElement = expectedElement;
		this.matchWith = matchWith;
		this.inputSuggestions = inputSuggestions;
	}

	public ParentedElement getExpectedElement() {
		return expectedElement;
	}

	public String getMatchWith() {
		return matchWith;
	}

	public List<InputSuggestion> getInputSuggestions() {
		return inputSuggestions;
	}
	
}
