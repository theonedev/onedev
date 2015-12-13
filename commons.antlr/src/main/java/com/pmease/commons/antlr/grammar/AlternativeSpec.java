package com.pmease.commons.antlr.grammar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class AlternativeSpec implements Serializable {

	private static final long serialVersionUID = 1L;

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
	
	@Override
	public String toString() {
		List<String> elementStrings = new ArrayList<>();
		for (ElementSpec element: elements)
			elementStrings.add(element.toString());
		return StringUtils.join(elementStrings, " ");
	}

}
