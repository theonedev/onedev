package com.pmease.commons.antlr.codeassist;

import java.util.List;

import com.pmease.commons.antlr.codeassist.parse.ParentChain;
import com.pmease.commons.antlr.codeassist.parse.ParsedElement;

public class ElementSuggestion {
	
	private final ParentChain parentChain;
	
	private final ParsedElement expectingElement;
	
	private final String matchWith;
	
	private final List<InputSuggestion> inputSuggestions;
	
	public ElementSuggestion(ParentChain parentChain, ParsedElement expectingElement, 
			String matchWith, List<InputSuggestion> inputSuggestions) {
		this.parentChain = parentChain;
		this.expectingElement = expectingElement;
		this.matchWith = matchWith;
		this.inputSuggestions = inputSuggestions;
	}

	public ParentChain getParentChain() {
		return parentChain;
	}

	public ParsedElement getExpectingElement() {
		return expectingElement;
	}

	public String getMatchWith() {
		return matchWith;
	}

	public List<InputSuggestion> getInputSuggestions() {
		return inputSuggestions;
	}
	
}
