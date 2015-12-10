package com.pmease.commons.antlr.codeassist.parse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.pmease.commons.antlr.codeassist.AssistStream;
import com.pmease.commons.antlr.codeassist.ElementSpec;
import com.pmease.commons.antlr.codeassist.RuleRefElementSpec;
import com.pmease.commons.antlr.codeassist.RuleSpec;
import com.pmease.commons.antlr.codeassist.ElementSpec.Multiplicity;

public class ParseState {
	
	private final int streamIndex;
	
	private final Set<ParseNode> nodes;
	
	private Set<CompletedRange> completedRanges;
	
	public Set<ParseNode> getNodes() {
		return nodes;
	}
	
	public ParseState(int streamIndex, Set<ParseNode> nodes) {
		this.streamIndex = streamIndex;
		this.nodes = nodes;
		for (ParseNode node: nodes) {
			if (node.isCompleted())
				completedRanges.add(new CompletedRange(node.getFromStreamIndex(), streamIndex));
		}
	}
	
	public void predict(AssistStream stream) {
		for (ParseNode node: Lists.newArrayList(nodes))
			predict(stream, node);
	}

	public ParseState scan(AssistStream stream) {
		Set<ParseNode> nextNodes = new HashSet<>();
		for (ParseNode node: nodes)
			scan(stream, node, nextNodes);
		
		return new ParseState(stream.getIndex(), nextNodes);
	}
	
	public void complete(List<ParseState> states) {
		for (ParseNode node: Lists.newArrayList(nodes))
			complete(node, states);
	}
	
	@Nullable
	private CompletedRange getCompletedRange(ParseNode node) {
		if (node.isCompleted())
			return new CompletedRange(node.getFromStreamIndex(), streamIndex);
		else
			return null;
	}
	
	private void predict(AssistStream stream, ParseNode node) {
		if (!node.isCompleted()) {
			ElementSpec nextElement = node.getElements().get(node.getNextElementIndex());
			if (nextElement instanceof RuleRefElementSpec) {
				RuleRefElementSpec ruleRefElement = (RuleRefElementSpec) nextElement;
				int tokenType = stream.getCurrentToken().getType();
				RuleSpec elementRule = ruleRefElement.getRule();
				if (elementRule.getFirstTokenTypes() == null 
						|| elementRule.getFirstTokenTypes().contains(tokenType)) {
					for (int i=0; i<elementRule.getAlternatives().size(); i++) {
						ParseNode predictedNode = new ParseNode(stream.getIndex(), elementRule, i, 0, false);
						if (addNode(predictedNode))
							predict(stream, predictedNode);
					}
				}
			}
			if (nextElement.matchesEmpty() || node.isNextElementMatched()) {
				ParseNode advancedNode = new ParseNode(stream.getIndex(), node.getRule(), 
						node.getAlternativeIndex(), node.getNextElementIndex()+1, false);
				if (addNode(advancedNode))
					predict(stream, advancedNode);
			}
		}
	}
	
	public void scan(AssistStream stream, ParseNode node, Set<ParseNode> nextNodes) {
		if (!node.isCompleted()) {
			ElementSpec nextElement = node.getElements().get(node.getNextElementIndex());
			int tokenType = stream.getCurrentToken().getType();
			if (nextElement.isToken(tokenType)) {
				ParseNode scanNode;
				if (nextElement.getMultiplicity() == Multiplicity.ONE 
						|| nextElement.getMultiplicity() == Multiplicity.ZERO_OR_ONE) {
					scanNode = new ParseNode(node.getFromStreamIndex(), node.getRule(), 
							node.getAlternativeIndex(), node.getNextElementIndex()+1, false);
				} else {
					scanNode = new ParseNode(node.getFromStreamIndex(), node.getRule(), 
							node.getAlternativeIndex(), node.getNextElementIndex(), true);
				}
				nodes.add(scanNode);
			}
		}
	}
	
	private boolean addNode(ParseNode node) {
		if (!nodes.contains(node)) {
			CompletedRange completedRange = getCompletedRange(node);
			if (completedRange != null) {
				if (!completedRanges.contains(completedRange)) {
					completedRanges.add(completedRange);
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
	
	private void complete(ParseNode node, List<ParseState> states) {
		if (node.isCompleted()) {
			ParseState state = states.get(node.getFromStreamIndex());
			for (ParseNode stateNode: state.getNodes()) {
				if (!stateNode.isCompleted()) {
					ElementSpec nextElement = stateNode.getElements().get(stateNode.getNextElementIndex());
					if (nextElement instanceof RuleRefElementSpec) {
						RuleRefElementSpec ruleRefElement = (RuleRefElementSpec) nextElement;
						if (ruleRefElement.getRuleName().equals(node.getRule().getName())) {
							ParseNode advancedNode;
							if (nextElement.getMultiplicity() == Multiplicity.ONE 
									|| nextElement.getMultiplicity() == Multiplicity.ZERO_OR_ONE) {
								advancedNode = new ParseNode(stateNode.getFromStreamIndex(), 
										stateNode.getRule(), stateNode.getAlternativeIndex(), 
										stateNode.getNextElementIndex()+1, false);
							} else {
								advancedNode = new ParseNode(stateNode.getFromStreamIndex(), 
										stateNode.getRule(), stateNode.getAlternativeIndex(), 
										stateNode.getNextElementIndex(), true);
							}
							advancedNode.getChildren().add(node);
							if (addNode(advancedNode))
								complete(advancedNode, states);
						}
					}
				}
			}
		}
	}
	
}
