package com.pmease.commons.antlr.codeassist;

import javax.annotation.Nullable;

public class Node {
	
	protected final Spec spec;
	
	protected final Node parent;
	
	protected final Node previous;
	
	/**
	 * Construct the node 
	 * 
	 * @param spec
	 * 			spec of the node
	 * @param parent
	 * 			parent of the node
	 * @param previous
	 * 			previous node, which is used to track all nodes in the parse tree
	 */
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

	@Override
	public String toString() {
		return "spec: " + spec;
	}
	
}
