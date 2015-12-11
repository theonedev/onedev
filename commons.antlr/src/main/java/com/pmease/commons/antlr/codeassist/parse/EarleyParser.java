package com.pmease.commons.antlr.codeassist.parse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.pmease.commons.antlr.codeassist.AssistStream;
import com.pmease.commons.antlr.codeassist.ElementSpec;
import com.pmease.commons.antlr.codeassist.ElementSpec.Multiplicity;
import com.pmease.commons.antlr.codeassist.RuleRefElementSpec;
import com.pmease.commons.antlr.codeassist.RuleSpec;

public class EarleyParser {

	private final RuleSpec rule;
	
	private final AssistStream stream;
	
	private final int initialStreamIndex;
	
	private final List<ParseState> states = new ArrayList<>();

	public EarleyParser(RuleSpec rule, AssistStream stream) {
		this.rule = rule;
		this.stream = stream;
		initialStreamIndex = stream.getIndex();
		
		Set<ParseNode> nodes = new HashSet<>();
		for (int i=0; i<rule.getAlternatives().size(); i++) 
			nodes.add(new ParseNode(initialStreamIndex, rule, i, 0, false));

		ParseState state = new ParseState(initialStreamIndex, nodes);
		states.add(state);

		while (!state.getNodes().isEmpty()) {
			for (ParseNode node: Lists.newArrayList(state.getNodes()))
				process(node, state);

			if (stream.isEof())
				break;
			
			nodes = new HashSet<>();
			for (ParseNode node: state.getNodes()) 
				scan(node, nodes);
			stream.increaseIndex();
			state = new ParseState(stream.getIndex(), nodes);
			states.add(state);
		}
	}
	
	private void process(ParseNode node, ParseState state) {
		if (!node.isCompleted()) { // predict
			ElementSpec nextElement = node.getElements().get(node.getNextElementIndex());
			if (nextElement instanceof RuleRefElementSpec) {
				RuleRefElementSpec ruleRefElement = (RuleRefElementSpec) nextElement;
				RuleSpec elementRule = ruleRefElement.getRule();
				for (int i=0; i<elementRule.getAlternatives().size(); i++) {
					ParseNode predictedNode = new ParseNode(stream.getIndex(), elementRule, i, 0, false);
					if (state.addNode(predictedNode))
						process(predictedNode, state);
				}
			}
			if (nextElement.matchesEmpty() || node.isNextElementMatched()) {
				ParseNode advancedNode = new ParseNode(node.getFromStreamIndex(), node.getRule(), 
						node.getAlternativeIndex(), node.getNextElementIndex()+1, false);
				advancedNode.getChildren().addAll(node.getChildren());
				if (state.addNode(advancedNode))
					process(advancedNode, state);
			}
		} else { // complete
			ParseState fromState = states.get(node.getFromStreamIndex() - initialStreamIndex);
			for (ParseNode fromNode: fromState.getNodes()) {
				if (!fromNode.isCompleted()) {
					ElementSpec nextElement = fromNode.getElements().get(fromNode.getNextElementIndex());
					if (nextElement instanceof RuleRefElementSpec) {
						RuleRefElementSpec ruleRefElement = (RuleRefElementSpec) nextElement;
						if (ruleRefElement.getRuleName().equals(node.getRule().getName())) {
							ParseNode advancedNode;
							if (nextElement.getMultiplicity() == Multiplicity.ONE 
									|| nextElement.getMultiplicity() == Multiplicity.ZERO_OR_ONE) {
								advancedNode = new ParseNode(fromNode.getFromStreamIndex(), 
										fromNode.getRule(), fromNode.getAlternativeIndex(), 
										fromNode.getNextElementIndex()+1, false);
							} else {
								advancedNode = new ParseNode(fromNode.getFromStreamIndex(), 
										fromNode.getRule(), fromNode.getAlternativeIndex(), 
										fromNode.getNextElementIndex(), true);
							}
							advancedNode.getChildren().addAll(fromNode.getChildren());
							advancedNode.getChildren().add(node);
							if (state.addNode(advancedNode))
								process(advancedNode, state);
						}
					}
				}
			}
		}
	}
	
	private void scan(ParseNode node, Set<ParseNode> nextNodes) {
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
				scanNode.getChildren().addAll(node.getChildren());
				nextNodes.add(scanNode);
			}
		}
	}
	
	public RuleSpec getRule() {
		return rule;
	}

	public AssistStream getStream() {
		return stream;
	}

	public List<ParseState> getStates() {
		return states;
	}
	
	public Set<ParseNode> getMatches() {
		Set<ParseNode> matches = new HashSet<>();
		if (states.size() == stream.size()+1-initialStreamIndex) {
			for (ParseNode node: states.get(states.size()-1).getNodes()) {
				if (node.getRule().getName().equals(rule.getName()) 
						&& node.getFromStreamIndex() == 0 && node.isCompleted()) {
					matches.add(node);
				}
			}
		}
		return matches;
	}
}
