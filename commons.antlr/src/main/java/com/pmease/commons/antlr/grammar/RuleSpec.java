package com.pmease.commons.antlr.grammar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class RuleSpec implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final List<AlternativeSpec> alternatives;
	
	public RuleSpec(String name, List<AlternativeSpec> alternatives) {
		this.name = name;
		this.alternatives = alternatives;
	}
	
	public String getName() {
		return name;
	}

	public List<AlternativeSpec> getAlternatives() {
		return alternatives;
	}

	@Override
	public String toString() {
		List<String> alternativeStrings = new ArrayList<>();
		for (AlternativeSpec alternative: alternatives)
			alternativeStrings.add(alternative.toString());
		return StringUtils.join(alternativeStrings, " | ");
	}

}
