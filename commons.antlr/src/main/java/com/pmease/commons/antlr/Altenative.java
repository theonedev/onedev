package com.pmease.commons.antlr;

import java.io.Serializable;
import java.util.List;

public class Altenative implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String label;
	
	private final List<Element> elements;
	
	public Altenative(String label, List<Element> elements) {
		this.label = label;
		this.elements = elements;
	}

	public String getLabel() {
		return label;
	}

	public List<Element> getElements() {
		return elements;
	}
	
}
