package com.pmease.commons.antlr.codeassist;

import java.util.List;

public class ElementSuggestion {
	
	private final Node node;
	
	private final List<InputSuggestion> inputSuggestions;
	
	public ElementSuggestion(Node node, List<InputSuggestion> inputSuggestions) {
		this.node = node;
		this.inputSuggestions = inputSuggestions;
	}

	public Node getNode() {
		return node;
	}

	public List<InputSuggestion> getInputSuggestions() {
		return inputSuggestions;
	}
	
}
