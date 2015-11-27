package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

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

	public List<Node> getChildNodes(Node parentNode) {
		List<Node> childNodes = new ArrayList<>();
		for (Node node: getAllNodes()) {
			if (node.getParent().equals(parentNode))
				childNodes.add(node);
		}
		return childNodes;
	}

	public Node getRootNode() {
		Node node = lastNode;
		while (node.getParent() != null) 
			node = node.getParent();
		
		return node;
	}

	public TokenNode getFirstTokenNode() {
		TokenNode tokenNode = lastNode;
		Node node = lastNode;
		while (node.getPrevious() != null) {
			node = node.getPrevious();
			if (node instanceof TokenNode)
				tokenNode = (TokenNode)node;
		}
		return tokenNode;
	}
	
	@Nullable
	public TokenNode getPreviousTokenNode(TokenNode tokenNode, int...tokenTypes) {
		Node node = tokenNode;
		while (node.getPrevious() != null) {
			node = node.getPrevious();
			if (node instanceof TokenNode) {
				tokenNode = (TokenNode) node;
				if (tokenTypes.length == 0) {
					return tokenNode;
				} else {
					for (int tokenType: tokenTypes) {
						if (tokenType == tokenNode.getToken().getType())
							return tokenNode;
					}
				}
			}
		}
		return null;
	}
	
}