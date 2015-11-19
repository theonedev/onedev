package com.pmease.commons.antlr.codeassist;

import java.util.List;

public class ElementSuggestion {
	
	private final Node elementNode;
	
	private final List<CaretAwareText> texts;
	
	public ElementSuggestion(Node elementNode, List<CaretAwareText> texts) {
		this.elementNode = elementNode;
		this.texts = texts;
	}

	public Node getElementNode() {
		return elementNode;
	}

	public List<CaretAwareText> getTexts() {
		return texts;
	}
	
}
