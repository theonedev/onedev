package com.pmease.commons.antlr.codeassist;

import java.util.List;

public class ElementSuggestion {
	
	private final ElementSpec spec;

	private final Node parent;
	
	private final List<CaretAwareText> texts;
	
	public ElementSuggestion(ElementSpec spec, Node parent, List<CaretAwareText> texts) {
		this.spec = spec;
		this.parent = parent;
		this.texts = texts;
	}

	public ElementSpec getSpec() {
		return spec;
	}

	public Node getParent() {
		return parent;
	}

	public List<CaretAwareText> getTexts() {
		return texts;
	}
	
}
