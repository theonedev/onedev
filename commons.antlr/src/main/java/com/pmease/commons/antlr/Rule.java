package com.pmease.commons.antlr;

import java.io.Serializable;
import java.util.List;

public class Rule implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int id;
	
	private final List<Altenative> altenatives;

	public Rule(int id, List<Altenative> altenatives) {
		this.id = id;
		this.altenatives = altenatives;
	}
	
	public int getId() {
		return id;
	}

	public List<Altenative> getAltenatives() {
		return altenatives;
	}

}
