package com.pmease.commons.antlr.codeassist;

public class ElementCompletion extends InputCompletion {

	private static final long serialVersionUID = 1L;
	
	private Node node;

	public ElementCompletion(Node node, int replaceBegin, int replaceEnd, String replaceContent, 
			int caret, String description) {
		super(replaceBegin, replaceEnd, replaceContent, caret, description);
		
		this.node = node;
	}

	public Node getNode() {
		return node;
	}

}