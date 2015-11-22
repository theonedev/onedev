package com.pmease.commons.antlr.codeassist;

import java.util.List;

public class ElementSuggestion {
	
	private final Node node;
	
	private final List<CaretAwareText> texts;
	
	public ElementSuggestion(Node node, List<CaretAwareText> texts) {
		this.node = node;
		this.texts = texts;
	}

	public Node getNode() {
		return node;
	}

	public List<CaretAwareText> getTexts() {
		return texts;
	}
	
}
