package com.pmease.commons.antlr.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.Token;

import com.pmease.commons.antlr.grammar.ElementSpec;
import com.pmease.commons.antlr.grammar.TerminalElementSpec;

public class State {
	
	private final EarleyParser parser;
	
	private final int endTokenIndex;
	
	private final Set<Node> nodes;
	
	private Set<RuleCompletion> ruleCompletions = new HashSet<>();
	
	public Set<Node> getNodes() {
		return nodes;
	}
	
	public State(EarleyParser parser, int tokenIndex, Set<Node> nodes) {
		this.parser = parser;
		this.endTokenIndex = tokenIndex;
		this.nodes = nodes;
		for (Node node: nodes) {
			if (node.isCompleted())
				ruleCompletions.add(new RuleCompletion(node.getRuleSpec().getName(), node.getBeginTokenIndex(), tokenIndex));
		}
	}
	
	@Nullable
	private RuleCompletion getRuleCompletion(Node node) {
		if (node.isCompleted())
			return new RuleCompletion(node.getRuleSpec().getName(), node.getBeginTokenIndex(), endTokenIndex);
		else
			return null;
	}
	

	public boolean addNode(Node node) {
		if (!nodes.contains(node)) {
			RuleCompletion ruleCompletion = getRuleCompletion(node);
			if (ruleCompletion != null) {
				if (!ruleCompletions.contains(ruleCompletion)) {
					ruleCompletions.add(ruleCompletion);
					nodes.add(node);
					return true;
				}
			} else {
				nodes.add(node);
				return true;
			}
		}
		return false;
	}

	public int getEndTokenIndex() {
		return endTokenIndex;
	}
	
	/**
	 * Get the token being scanned to create this state.
	 * 
	 * @return
	 * 			the token being scanned to create this state, or <tt>null</tt> for initial state
	 */
	@Nullable
	public Token getInitiatingToken() {
		if (endTokenIndex > 0)
			return parser.getTokens().get(endTokenIndex-1);
		else
			return null;
	}
	
	public List<Node> getMatches(String ruleName) {
		List<Node> matches = new ArrayList<>();
		for (Node node: nodes) {
			if (node.getRuleSpec().getName().equals(ruleName) 
					&& node.getBeginTokenIndex() == 0 && node.isCompleted()) {
				matches.add(node);
			}
		}
		return matches;
	}
	
	public List<Node> getNodesExpectingTerminal() {
		List<Node> nodesExpectingTerminal = new ArrayList<>();
		for (Node node: nodes) {
			ElementSpec expectingSpec = node.getExpectedElementSpec();
			if (expectingSpec instanceof TerminalElementSpec)
				nodesExpectingTerminal.add(node);
		}
		return nodesExpectingTerminal;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (Node node: nodes)
			buffer.append(node).append("\n");
		return buffer.toString();
	}
	
}
