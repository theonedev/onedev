package com.pmease.commons.antlr.parsetree;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import com.pmease.commons.antlr.grammarabstraction.GrammarMember;

public class Node implements Serializable {

	private static final long serialVersionUID = 1L;

	private final GrammarMember grammarMember;
	
	private final Node parent;
	
	private final List<Node> children;
	
	public Node(GrammarMember grammarMember, Node parent, List<Node> children) {
		this.grammarMember = grammarMember;
		this.parent = parent;
		this.children = children;
	}

	public GrammarMember getGrammarMember() {
		return grammarMember;
	}

	@Nullable
	public Node getParent() {
		return parent;
	}

	public List<Node> getChildren() {
		return children;
	}
	
}
