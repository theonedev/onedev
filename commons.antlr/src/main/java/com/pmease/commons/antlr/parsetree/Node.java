package com.pmease.commons.antlr.parsetree;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.pmease.commons.antlr.grammarspec.Spec;

public class Node implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Spec spec;
	
	private final Node parent;
	
	public Node(Spec spec, Node parent) {
		this.spec = spec;
		this.parent = parent;
	}

	public Spec getSpec() {
		return spec;
	}

	@Nullable
	public Node getParent() {
		return parent;
	}

}
