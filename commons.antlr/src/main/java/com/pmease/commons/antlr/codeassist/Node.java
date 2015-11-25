package com.pmease.commons.antlr.codeassist;

import javax.annotation.Nullable;

public class Node {
	
	protected final Spec spec;
	
	protected final Node parent;
	
	protected final Node previous;
	
	public Node(Spec spec, @Nullable Node parent, @Nullable Node previous) {
		this.spec = spec;
		this.parent = parent;
		this.previous = previous;
	}

	public Spec getSpec() {
		return spec;
	}

	@Nullable
	public Node getParent() {
		return parent;
	}

	@Nullable
	public Node getPrevious() {
		return previous;
	}
	
}
