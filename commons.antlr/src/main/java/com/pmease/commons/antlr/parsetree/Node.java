package com.pmease.commons.antlr.parsetree;

import com.pmease.commons.antlr.grammarspec.Spec;

public abstract class Node {
	
	private final Spec spec;
	
	private final Node parent;
	
	public Node(Spec spec, Node parent) {
		this.spec = spec;
		this.parent = parent;
	}

	public Spec getSpec() {
		return spec;
	}

	public Node getParent() {
		return parent;
	}
	
}
