package com.pmease.commons.antlr;

import java.io.Serializable;
import java.util.List;

public class Rule implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final List<Altenative> altenatives;

	public Rule(String name, List<Altenative> altenatives) {
		this.name = name;
		this.altenatives = altenatives;
	}
	
	public String getName() {
		return name;
	}

	public List<Altenative> getAltenatives() {
		return altenatives;
	}

}
