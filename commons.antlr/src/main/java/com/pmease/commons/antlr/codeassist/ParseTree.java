package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Parse tree represents parse state. It can be partial if only part of the 
 * spec matches in the stream. Suggestion providers can use this to check 
 * current state of the parsing, to provide meaningful suggestions if 
 * necessary. 
 * 
 * @author robin
 *
 */
public class ParseTree {
	
	private List<Node> nodes;
	
	public ParseTree(List<Node> nodes) {
		this.nodes = nodes;
	}

	public TokenNode getLastNode() {
		return (TokenNode) nodes.get(nodes.size()-1);
	}

	public List<Node> getChildNodes(Node parentNode) {
		List<Node> childNodes = new ArrayList<>();
		for (Node node: nodes) {
			if (node.getParent().equals(parentNode))
				childNodes.add(node);
		}
		return childNodes;
	}

	public Node getRootNode() {
		Node node = getLastNode();
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
		Node node = getLastNode();
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
		Node node = getLastNode();
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
		for (Node node: nodes) {
			if (node instanceof TokenNode)
				return (TokenNode) node;
		}
		throw new IllegalStateException();
	}
	
	@Nullable
	public TokenNode getFirstTokenNode(Node parentNode) {
		for (Node node: nodes) {
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
		List<Node> nodes = new ArrayList<>(this.nodes);
		Collections.reverse(nodes);
		return getNextTokenNode(nodes, tokenNode, tokenTypes);
	}
	
	@Nullable
	public TokenNode getNextTokenNode(List<Node> nodes, TokenNode tokenNode, int...tokenTypes) {
		Set<Integer> tokenTypeSet = new HashSet<>();
		for (int tokenType: tokenTypes)
			tokenTypeSet.add(tokenType);
		boolean found = false;
		for (Node node: nodes) {
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
		return getNextTokenNode(nodes, tokenNode, tokenTypes);
	}
	
	@Nullable
	public static ParseTree of(TokenNode tokenNode) {
		tokenNode = tokenNode.peel();
		if (tokenNode == null)
			return null;
		
		List<Node> nodes = new ArrayList<>();
		nodes.add(tokenNode);
		Node previousNode = tokenNode.getPrevious();
		while (previousNode != null) {
			if (!(previousNode instanceof TokenNode) 
					|| !(((TokenNode) previousNode).getToken() instanceof FakedToken)) {
				nodes.add(previousNode);
			}
			previousNode = previousNode.getPrevious();
		}
		Collections.reverse(nodes);
		return new ParseTree(nodes);
	}
	
}