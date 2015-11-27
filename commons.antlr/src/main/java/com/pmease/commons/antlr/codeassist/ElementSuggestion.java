package com.pmease.commons.antlr.codeassist;

import java.util.List;

import javax.annotation.Nullable;

public class ElementSuggestion {
	
	private final ParseTree parseTree; 
	
	private final String matchWith;
	
	private final Node node;
	
	private final List<InputSuggestion> inputSuggestions;
	
	public ElementSuggestion(@Nullable ParseTree parseTree, Node node, String matchWith, 
			List<InputSuggestion> inputSuggestions) {
		this.parseTree = parseTree;
		this.node = node;
		this.matchWith = matchWith;
		this.inputSuggestions = inputSuggestions;
	}

	@Nullable
	public ParseTree getParseTree() {
		return parseTree;
	}

	public Node getNode() {
		return node;
	}

	public String getMatchWith() {
		return matchWith;
	}

	public List<InputSuggestion> getInputSuggestions() {
		return inputSuggestions;
	}
	
}
