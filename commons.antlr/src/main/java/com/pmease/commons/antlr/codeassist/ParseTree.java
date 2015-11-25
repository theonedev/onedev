package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

public class ParseTree {
	
	private final TokenNode lastNode;
	
	private transient List<Node> allNodes;
	
	public ParseTree(TokenNode lastNode) {
		this.lastNode = lastNode;
	}

	public TokenNode getLastNode() {
		return lastNode;
	}

	public List<Node> getAllNodes() {
		if (allNodes == null) {
			allNodes = new ArrayList<>();
			allNodes.add(lastNode);
			
			Node previous = lastNode.getPrevious();
			while (previous != null) {
				allNodes.add(previous);
				previous = previous.getPrevious();
			}
		}
		return allNodes;
	}

	public List<Node> getChildNodes(Node parent) {
		List<Node> childNodes = new ArrayList<>();
		for (Node node: getAllNodes()) {
			if (node.getParent().equals(parent))
				childNodes.add(node);
		}
		return childNodes;
	}
	
}