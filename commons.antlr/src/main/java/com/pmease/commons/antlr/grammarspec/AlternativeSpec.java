package com.pmease.commons.antlr.grammarspec;

import java.util.List;

public class AlternativeSpec implements Spec {

	private final String label;
	
	private final List<ElementSpec> elements;
	
	public AlternativeSpec(String label, List<ElementSpec> elements) {
		this.label = label;
		this.elements = elements;
	}

	public String getLabel() {
		return label;
	}

	public List<ElementSpec> getElements() {
		return elements;
	}
	
}
