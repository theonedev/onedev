package com.pmease.commons.antlr.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.Token;

import com.google.common.collect.Lists;
import com.pmease.commons.antlr.grammar.ElementSpec;
import com.pmease.commons.antlr.grammar.RuleRefElementSpec;
import com.pmease.commons.antlr.grammar.RuleSpec;
import com.pmease.commons.antlr.grammar.TerminalElementSpec;
import com.pmease.commons.antlr.grammar.ElementSpec.Multiplicity;

public class EarleyParser {

	private final RuleSpec rule;
	
	private final List<Token> tokens;
	
	private int tokenIndex = 0;
	
	private final List<State> states = new ArrayList<>();

	public EarleyParser(RuleSpec rule, List<Token> tokens) {
		this.rule = rule;
		this.tokens = tokens;
		
		Set<Node> nodes = new HashSet<>();
		for (int i=0; i<rule.getAlternatives().size(); i++) 
			nodes.add(new Node(tokenIndex, rule, i, 0, false, new ArrayList<Element>()));

		while (!nodes.isEmpty()) {
			State state = new State(tokenIndex, nodes);
			states.add(state);
			for (Node node: Lists.newArrayList(state.getNodes()))
				process(node, state);

			if (tokenIndex == tokens.size())
				break;
			
			nodes = new HashSet<>();
			for (Node node: state.getNodes()) 
				scan(node, nodes);
			tokenIndex++;
		}
	}
	
	private void process(Node node, State state) {
		if (!node.isCompleted()) { // predict
			ElementSpec nextElement = node.getElementSpecs().get(node.getExpectedElementSpecIndex());
			if (nextElement instanceof RuleRefElementSpec) {
				RuleRefElementSpec ruleRefElement = (RuleRefElementSpec) nextElement;
				RuleSpec elementRule = ruleRefElement.getRule();
				for (int i=0; i<elementRule.getAlternatives().size(); i++) {
					Node predictedNode = new Node(tokenIndex, 
							elementRule, i, 0, false, new ArrayList<Element>());
					if (state.addNode(predictedNode))
						process(predictedNode, state);
				}
			}
			if (nextElement.getMultiplicity() == Multiplicity.ZERO_OR_MORE 
					|| nextElement.getMultiplicity() == Multiplicity.ZERO_OR_ONE 
					|| node.isExpectedElementSpecMatchedOnce()) {
				Node advancedNode = new Node(node.getBeginTokenIndex(), node.getRuleSpec(), 
						node.getAlternativeSpecIndex(), node.getExpectedElementSpecIndex()+1, false, 
						new ArrayList<>(node.getElements()));
				if (state.addNode(advancedNode))
					process(advancedNode, state);
			}
		} else { // complete
			State startState = states.get(node.getBeginTokenIndex());
			for (Node startNode: startState.getNodes()) {
				if (!startNode.isCompleted()) {
					ElementSpec nextElement = startNode.getExpectedElementSpec();
					if (nextElement instanceof RuleRefElementSpec) {
						RuleRefElementSpec ruleRefElement = (RuleRefElementSpec) nextElement;
						if (ruleRefElement.getRuleName().equals(node.getRuleSpec().getName())) {
							Node advancedNode;
							List<Element> elements = new ArrayList<>(startNode.getElements());
							if (!node.getElements().isEmpty())
								elements.add(new Element(this, ruleRefElement, state.getEndTokenIndex(), node));
							if (nextElement.getMultiplicity() == Multiplicity.ONE 
									|| nextElement.getMultiplicity() == Multiplicity.ZERO_OR_ONE) {
								advancedNode = new Node(startNode.getBeginTokenIndex(), 
										startNode.getRuleSpec(), startNode.getAlternativeSpecIndex(), 
										startNode.getExpectedElementSpecIndex()+1, false, elements);
							} else {
								advancedNode = new Node(startNode.getBeginTokenIndex(), 
										startNode.getRuleSpec(), startNode.getAlternativeSpecIndex(), 
										startNode.getExpectedElementSpecIndex(), true, elements);
							}
							if (state.addNode(advancedNode))
								process(advancedNode, state);
						}
					}
				}
			}
		}
	}
	
	private void scan(Node node, Set<Node> nextNodes) {
		if (!node.isCompleted()) {
			ElementSpec nextElement = node.getElementSpecs().get(node.getExpectedElementSpecIndex());
			int tokenType = tokens.get(tokenIndex).getType();
			if ((nextElement instanceof TerminalElementSpec) 
					&& ((TerminalElementSpec)nextElement).isToken(tokenType)) {
				Node scanNode;
				List<Element> elements = new ArrayList<>(node.getElements());
				elements.add(new Element(this, nextElement, tokenIndex+1, null));
				if (nextElement.getMultiplicity() == Multiplicity.ONE 
						|| nextElement.getMultiplicity() == Multiplicity.ZERO_OR_ONE) {
					scanNode = new Node(node.getBeginTokenIndex(), node.getRuleSpec(), 
							node.getAlternativeSpecIndex(), node.getExpectedElementSpecIndex()+1, 
							false, elements);
				} else {
					scanNode = new Node(node.getBeginTokenIndex(), node.getRuleSpec(), 
							node.getAlternativeSpecIndex(), node.getExpectedElementSpecIndex(), 
							true, elements);
				}
				nextNodes.add(scanNode);
			}
		}
	}
	
	public List<State> getStates() {
		return states;
	}
	
	public RuleSpec getRule() {
		return rule;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public Set<Node> getMatches() {
		if (states.size() == tokens.size()+1) 
			return states.get(tokens.size()).getMatches(rule.getName());
		else
			return new HashSet<>();
	}
	
	public boolean matches() {
		return !getMatches().isEmpty();
	}
	
	@Nullable
	public Token getLastMatchedToken() {
		int endOfMatch = getEndOfMatch();
		if (endOfMatch > 0)
			return tokens.get(endOfMatch-1);
		else
			return null;
	}
	
	public List<Token> getMatchedTokens() {
		int endOfMatch = getEndOfMatch();
		if (endOfMatch > 0)
			return tokens.subList(0, endOfMatch);
		else
			return new ArrayList<>();
	}
	
	public int getEndOfMatch() {
		for (int i=states.size()-1; i>=0; i--) {
			State state = states.get(i);
			if (!state.getMatches(rule.getName()).isEmpty())
				return i;
		}
		return -1;
	}
	
}
