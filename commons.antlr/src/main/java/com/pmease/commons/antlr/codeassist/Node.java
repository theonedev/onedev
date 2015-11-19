package com.pmease.commons.antlr.codeassist;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.Token;

public class Node {
	
	protected final Spec spec;
	
	protected final Node parent;
	
	protected final Token start;
	
	public Node(Spec spec, Node parent, Token start) {
		this.spec = spec;
		this.parent = parent;
		this.start = start;
	}

	public Node(Spec spec, Node parent) {
		this(spec, parent, null);
	}
	
	public Spec getSpec() {
		return spec;
	}

	@Nullable
	public Node getParent() {
		return parent;
	}

	@Nullable
	public Token getStart() {
		return start;
	}
	
}
