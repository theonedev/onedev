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

	public List<Node> getChildren(Node parent, boolean recursive) {
		List<Node> children = new ArrayList<>();
		for (Node node: nodes) {
			if (recursive) {
				if (isAncestor(parent, node))
					children.add(node);
			} else if (node.getParent().equals(parent)) {
				children.add(node);
			}
		}
		return children;
	}
	
	public boolean isAncestor(Node parent, Node child) {
		Node current = child.getParent();
		while (current != null) {
			if (current.equals(parent))
				return true;
			current = current.getParent();
		}
		return false;
	}

	public Node getRoot() {
		Node node = getLastNode();
		while (node.getParent() != null) 
			node = node.getParent();
		
		return node;
	}
	
	public String getText(Node node) {
		if (node instanceof TokenNode) {
			return ((TokenNode) node).getToken().getText();
		} else {
			StringBuffer buffer = new StringBuffer();
			for (Node child: getChildren(node, true)) { 
				if (child instanceof TokenNode) 
					buffer.append(((TokenNode)child).getToken().getText());
			}
			return buffer.toString();
		}
	}
	
	public List<Node> getChildrenByRuleName(Node parent, String ruleName, boolean recursive) {
		List<Node> children = new ArrayList<>();
		for (Node child: getChildren(parent, recursive)) {
			if (child.getSpec() instanceof RuleRefElementSpec) {
				RuleRefElementSpec ruleRefElementSpec = (RuleRefElementSpec) child.getSpec();
				if (ruleRefElementSpec.getRuleName().equals(ruleName))
					children.add(child);
			} else if (child.getSpec() instanceof LexerRuleRefElementSpec) {
				LexerRuleRefElementSpec lexerRuleRefElementSpec = (LexerRuleRefElementSpec) child.getSpec();
				if (lexerRuleRefElementSpec.getRuleName().equals(ruleName))
					children.add(child);
			} else if (child.getSpec() instanceof RuleSpec) {
				RuleSpec ruleSpec = (RuleSpec) child.getSpec();
				if (ruleSpec.getName().equals(ruleName))
					children.add(child);
			}
		}
		return children;
	}

	public List<Node> getChildrenByLabel(Node parent, String label, boolean recursive) {
		List<Node> children = new ArrayList<>();
		for (Node child: getChildren(parent, recursive)) {
			if (child.getSpec() instanceof ElementSpec) {
				ElementSpec elementSpec = (ElementSpec) child.getSpec();
				if (label.equals(elementSpec.getLabel()))
					children.add(child);
			} else if (child.getSpec() instanceof AlternativeSpec) {
				AlternativeSpec alternativeSpec = (AlternativeSpec) child.getSpec();
				if (label.equals(alternativeSpec.getLabel()))
					children.add(child);
			}
		}
		return children;
	}

	@Nullable
	public Node findParentByRuleName(Node child, String ruleName) {
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
	public Node findParentByLabel(Node child, String label) {
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