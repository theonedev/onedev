package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	
	public List<Node> getChildNodeByRuleName(Node parentNode, String ruleName) {
		List<Node> childNodes = new ArrayList<>();
		for (Node childNode: getChildNodes(parentNode)) {
			if (childNode.getSpec() instanceof RuleRefElementSpec) {
				RuleRefElementSpec ruleRefElementSpec = (RuleRefElementSpec) childNode.getSpec();
				if (ruleRefElementSpec.getRuleName().equals(ruleName))
					childNodes.add(childNode);
			} else if (childNode.getSpec() instanceof LexerRuleRefElementSpec) {
				LexerRuleRefElementSpec lexerRuleRefElementSpec = (LexerRuleRefElementSpec) childNode.getSpec();
				if (lexerRuleRefElementSpec.getRuleName().equals(ruleName))
					childNodes.add(childNode);
			} else if (childNode.getSpec() instanceof RuleSpec) {
				RuleSpec ruleSpec = (RuleSpec) childNode.getSpec();
				if (ruleSpec.getName().equals(ruleName))
					childNodes.add(childNode);
			}
		}
		return childNodes;
	}

	public List<Node> getChildNodeByLabel(Node parentNode, String label) {
		List<Node> childNodes = new ArrayList<>();
		for (Node childNode: getChildNodes(parentNode)) {
			if (childNode.getSpec() instanceof ElementSpec) {
				ElementSpec elementSpec = (ElementSpec) childNode.getSpec();
				if (label.equals(elementSpec.getLabel()))
					childNodes.add(childNode);
			} else if (childNode.getSpec() instanceof AlternativeSpec) {
				AlternativeSpec alternativeSpec = (AlternativeSpec) childNode.getSpec();
				if (label.equals(alternativeSpec.getLabel()))
					childNodes.add(childNode);
			}
		}
		return childNodes;
	}

	@Nullable
	public Node findParentNodeByRuleName(Node childNode, String ruleName) {
		Node node = lastNode;
		while (node.getParent() != null) { 
			node = node.getParent();
			if (node.getSpec() instanceof RuleSpec) {
				RuleSpec ruleSpec = (RuleSpec) node.getSpec();
				if (ruleSpec.getName().equals(ruleName))
					return node;
			} else if (node.getSpec() instanceof RuleRefElementSpec) {
				RuleRefElementSpec ruleRefElementSpec = (RuleRefElementSpec) node.getSpec();
				if (ruleRefElementSpec.equals(ruleName))
					return node;
			} else if (node.getSpec() instanceof LexerRuleRefElementSpec) {
				LexerRuleRefElementSpec lexerRuleRefElementSpec = (LexerRuleRefElementSpec) node.getSpec();
				if (lexerRuleRefElementSpec.equals(ruleName))
					return node;
			}
		}
		
		return null;
	}
	
	@Nullable
	public Node findParentNodeByLabel(Node childNode, String label) {
		Node node = lastNode;
		while (node.getParent() != null) { 
			node = node.getParent();
			if (node.getSpec() instanceof ElementSpec) {
				ElementSpec elementSpec = (ElementSpec) node.getSpec();
				if (label.equals(elementSpec.getLabel()))
					return node;
			} else if (node.getSpec() instanceof AlternativeSpec) {
				AlternativeSpec alternativeSpec = (AlternativeSpec) node.getSpec();
				if (label.equals(alternativeSpec.getLabel()))
					return node;
			}
		}
		
		return null;
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
	public TokenNode getFirstTokenNode(Node parentNode) {
		List<Node> allNodes = getAllNodes();
		Collections.reverse(allNodes);
		for (Node node: allNodes) {
			if (node instanceof TokenNode) {
				TokenNode tokenNode = (TokenNode) node;
				Node currentNode = tokenNode;
				while (currentNode.getParent() != null) { 
					currentNode = currentNode.getParent();
					if (currentNode.equals(parentNode))
						return tokenNode;
				}
			}
		}
		return null;
	}

	@Nullable
	public TokenNode getPreviousTokenNode(TokenNode tokenNode, int...tokenTypes) {
		return getNextTokenNode(getAllNodes(), tokenNode, tokenTypes);
	}
	
	@Nullable
	public TokenNode getNextTokenNode(List<Node> allNodes, TokenNode tokenNode, int...tokenTypes) {
		Set<Integer> tokenTypeSet = new HashSet<>();
		for (int tokenType: tokenTypes)
			tokenTypeSet.add(tokenType);
		boolean found = false;
		for (Node node: allNodes) {
			if (node.equals(tokenNode)) {
				found = true;
			} else if (found && (node instanceof TokenNode) 
					&& tokenTypeSet.contains(((TokenNode)node).getToken().getType())) {
				return (TokenNode) node;
			}
		}
		return null;
	}
	
	@Nullable
	public TokenNode getNextTokenNode(TokenNode tokenNode, int...tokenTypes) {
		List<Node> allNodes = getAllNodes();
		Collections.reverse(allNodes);
		return getNextTokenNode(allNodes, tokenNode, tokenTypes);
	}
	
}