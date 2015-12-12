package com.pmease.commons.antlr.codeassist.parse;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.pmease.commons.antlr.codeassist.ElementSpec;
import com.pmease.commons.antlr.codeassist.TerminalElementSpec;

public class ParseState {
	
	private final int nextTokenIndex;
	
	private final Set<ParseNode> nodes;
	
	private Set<RuleCompletion> ruleCompletions = new HashSet<>();
	
	public Set<ParseNode> getNodes() {
		return nodes;
	}
	
	public ParseState(int tokenIndex, Set<ParseNode> nodes) {
		this.nextTokenIndex = tokenIndex;
		this.nodes = nodes;
		for (ParseNode node: nodes) {
			if (node.isCompleted())
				ruleCompletions.add(new RuleCompletion(node.getRuleSpec().getName(), node.getStartTokenIndex(), tokenIndex));
		}
	}
	
	@Nullable
	private RuleCompletion getRuleCompletion(ParseNode node) {
		if (node.isCompleted())
			return new RuleCompletion(node.getRuleSpec().getName(), node.getStartTokenIndex(), nextTokenIndex);
		else
			return null;
	}
	

	public boolean addNode(ParseNode node) {
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

	public int getNextTokenIndex() {
		return nextTokenIndex;
	}
	
	public Set<ParseNode> getMatches(String ruleName) {
		Set<ParseNode> matches = new HashSet<>();
		for (ParseNode node: nodes) {
			if (node.getRuleSpec().getName().equals(ruleName) 
					&& node.getStartTokenIndex() == 0 && node.isCompleted()) {
				matches.add(node);
			}
		}
		return matches;
	}
	
	public Set<ParseNode> getNodesExpectingTerminal() {
		Set<ParseNode> nodesExpectingTerminal = new HashSet<>();
		for (ParseNode node: nodes) {
			ElementSpec expectingSpec = node.getExpectedElementSpec();
			if (expectingSpec instanceof TerminalElementSpec)
				nodesExpectingTerminal.add(node);
		}
		return nodesExpectingTerminal;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (ParseNode node: nodes)
			buffer.append(node).append("\n");
		return buffer.toString();
	}
	
}
