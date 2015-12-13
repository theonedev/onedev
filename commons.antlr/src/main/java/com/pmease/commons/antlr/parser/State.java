package com.pmease.commons.antlr.parser;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.pmease.commons.antlr.grammar.ElementSpec;
import com.pmease.commons.antlr.grammar.TerminalElementSpec;

public class State {
	
	private final int endTokenIndex;
	
	private final Set<Node> nodes;
	
	private Set<RuleCompletion> ruleCompletions = new HashSet<>();
	
	public Set<Node> getNodes() {
		return nodes;
	}
	
	public State(int tokenIndex, Set<Node> nodes) {
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
	
	public Set<Node> getMatches(String ruleName) {
		Set<Node> matches = new HashSet<>();
		for (Node node: nodes) {
			if (node.getRuleSpec().getName().equals(ruleName) 
					&& node.getBeginTokenIndex() == 0 && node.isCompleted()) {
				matches.add(node);
			}
		}
		return matches;
	}
	
	public Set<Node> getNodesExpectingTerminal() {
		Set<Node> nodesExpectingTerminal = new HashSet<>();
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
