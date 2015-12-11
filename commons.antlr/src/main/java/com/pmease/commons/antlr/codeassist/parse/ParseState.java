package com.pmease.commons.antlr.codeassist.parse;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

public class ParseState {
	
	private final int streamIndex;
	
	private final Set<ParseNode> nodes;
	
	private Set<RuleCompletion> ruleCompletions = new HashSet<>();
	
	public Set<ParseNode> getNodes() {
		return nodes;
	}
	
	public ParseState(int streamIndex, Set<ParseNode> nodes) {
		this.streamIndex = streamIndex;
		this.nodes = nodes;
		for (ParseNode node: nodes) {
			if (node.isCompleted())
				ruleCompletions.add(new RuleCompletion(node.getRule().getName(), node.getFromStreamIndex(), streamIndex));
		}
	}
	
	@Nullable
	private RuleCompletion getRuleCompletion(ParseNode node) {
		if (node.isCompleted())
			return new RuleCompletion(node.getRule().getName(), node.getFromStreamIndex(), streamIndex);
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

	public int getStreamIndex() {
		return streamIndex;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (ParseNode node: nodes)
			buffer.append(node).append("\n");
		return buffer.toString();
	}
	
}
