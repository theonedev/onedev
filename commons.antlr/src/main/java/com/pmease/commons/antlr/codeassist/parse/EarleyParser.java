package com.pmease.commons.antlr.codeassist.parse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.Token;

import com.google.common.collect.Lists;
import com.pmease.commons.antlr.codeassist.ElementSpec;
import com.pmease.commons.antlr.codeassist.ElementSpec.Multiplicity;
import com.pmease.commons.antlr.codeassist.RuleRefElementSpec;
import com.pmease.commons.antlr.codeassist.RuleSpec;
import com.pmease.commons.antlr.codeassist.TerminalElementSpec;

public class EarleyParser {

	private final RuleSpec rule;
	
	private final List<Token> tokens;
	
	private int tokenIndex = 0;
	
	private final List<ParseState> states = new ArrayList<>();

	public EarleyParser(RuleSpec rule, List<Token> tokens) {
		this.rule = rule;
		this.tokens = tokens;
		
		Set<ParseNode> nodes = new HashSet<>();
		for (int i=0; i<rule.getAlternatives().size(); i++) 
			nodes.add(new ParseNode(tokenIndex, rule, i, 0, false, new ArrayList<ParsedElement>()));

		while (!nodes.isEmpty()) {
			ParseState state = new ParseState(tokenIndex, nodes);
			states.add(state);
			for (ParseNode node: Lists.newArrayList(state.getNodes()))
				process(node, state);

			if (tokenIndex == tokens.size())
				break;
			
			nodes = new HashSet<>();
			for (ParseNode node: state.getNodes()) 
				scan(node, nodes);
			tokenIndex++;
		}
	}
	
	private void process(ParseNode node, ParseState state) {
		if (!node.isCompleted()) { // predict
			ElementSpec nextElement = node.getElementSpecs().get(node.getExpectedElementSpecIndex());
			if (nextElement instanceof RuleRefElementSpec) {
				RuleRefElementSpec ruleRefElement = (RuleRefElementSpec) nextElement;
				RuleSpec elementRule = ruleRefElement.getRule();
				for (int i=0; i<elementRule.getAlternatives().size(); i++) {
					ParseNode predictedNode = new ParseNode(tokenIndex, 
							elementRule, i, 0, false, new ArrayList<ParsedElement>());
					if (state.addNode(predictedNode))
						process(predictedNode, state);
				}
			}
			if (nextElement.getMultiplicity() == Multiplicity.ZERO_OR_MORE 
					|| nextElement.getMultiplicity() == Multiplicity.ZERO_OR_ONE 
					|| node.isExpectedElementSpecMatchedOnce()) {
				ParseNode advancedNode = new ParseNode(node.getStartTokenIndex(), node.getRuleSpec(), 
						node.getAlternativeSpecIndex(), node.getExpectedElementSpecIndex()+1, false, 
						new ArrayList<>(node.getParsedElements()));
				if (state.addNode(advancedNode))
					process(advancedNode, state);
			}
		} else { // complete
			ParseState startState = states.get(node.getStartTokenIndex());
			for (ParseNode startNode: startState.getNodes()) {
				if (!startNode.isCompleted()) {
					ElementSpec nextElement = startNode.getExpectedElementSpec();
					if (nextElement instanceof RuleRefElementSpec) {
						RuleRefElementSpec ruleRefElement = (RuleRefElementSpec) nextElement;
						if (ruleRefElement.getRuleName().equals(node.getRuleSpec().getName())) {
							ParseNode advancedNode;
							List<ParsedElement> parsedElements = new ArrayList<>(startNode.getParsedElements());
							if (!node.getParsedElements().isEmpty())
								parsedElements.add(new ParsedElement(ruleRefElement, state.getNextTokenIndex(), node));
							if (nextElement.getMultiplicity() == Multiplicity.ONE 
									|| nextElement.getMultiplicity() == Multiplicity.ZERO_OR_ONE) {
								advancedNode = new ParseNode(startNode.getStartTokenIndex(), 
										startNode.getRuleSpec(), startNode.getAlternativeSpecIndex(), 
										startNode.getExpectedElementSpecIndex()+1, false, parsedElements);
							} else {
								advancedNode = new ParseNode(startNode.getStartTokenIndex(), 
										startNode.getRuleSpec(), startNode.getAlternativeSpecIndex(), 
										startNode.getExpectedElementSpecIndex(), true, parsedElements);
							}
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
			ElementSpec nextElement = node.getElementSpecs().get(node.getExpectedElementSpecIndex());
			int tokenType = tokens.get(tokenIndex).getType();
			if ((nextElement instanceof TerminalElementSpec) 
					&& ((TerminalElementSpec)nextElement).isToken(tokenType)) {
				ParseNode scanNode;
				List<ParsedElement> parsedElements = new ArrayList<>(node.getParsedElements());
				parsedElements.add(new ParsedElement(nextElement, tokenIndex+1, null));
				if (nextElement.getMultiplicity() == Multiplicity.ONE 
						|| nextElement.getMultiplicity() == Multiplicity.ZERO_OR_ONE) {
					scanNode = new ParseNode(node.getStartTokenIndex(), node.getRuleSpec(), 
							node.getAlternativeSpecIndex(), node.getExpectedElementSpecIndex()+1, 
							false, parsedElements);
				} else {
					scanNode = new ParseNode(node.getStartTokenIndex(), node.getRuleSpec(), 
							node.getAlternativeSpecIndex(), node.getExpectedElementSpecIndex(), 
							true, parsedElements);
				}
				nextNodes.add(scanNode);
			}
		}
	}
	
	public List<ParseState> getStates() {
		return states;
	}
	
	public RuleSpec getRule() {
		return rule;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public Set<ParseNode> getMatches() {
		if (states.size() == tokens.size()+1) 
			return states.get(tokens.size()).getMatches(rule.getName());
		else
			return new HashSet<>();
	}
	
	public int getMatchDistance() {
		for (int i=states.size()-1; i>=0; i--) {
			ParseState state = states.get(i);
			if (!state.getMatches(rule.getName()).isEmpty())
				return i;
		}
		return -1;
	}
	
	public List<ParsedElement> assumeCompleted(ParseNode node, int stopTokenIndex) {
		List<ParsedElement> rootElements = new ArrayList<>();
		if (node.getStartTokenIndex() == 0 && node.getRuleSpec().getName().equals(rule.getName())) {
			rootElements.add(new ParsedElement(null, stopTokenIndex, node));
		} else {
			ParseState startState = states.get(node.getStartTokenIndex());
			for (ParseNode startNode: startState.getNodes()) {
				if (!startNode.isCompleted()) {
					ElementSpec nextElement = startNode.getExpectedElementSpec();
					if (nextElement instanceof RuleRefElementSpec) {
						RuleRefElementSpec ruleRefElement = (RuleRefElementSpec) nextElement;
						if (ruleRefElement.getRuleName().equals(node.getRuleSpec().getName())) {
							List<ParsedElement> parsedElements = new ArrayList<>(startNode.getParsedElements());
							parsedElements.add(new ParsedElement(ruleRefElement, stopTokenIndex, node));
							ParseNode parentNode = new ParseNode(startNode.getStartTokenIndex(), 
									startNode.getRuleSpec(), startNode.getAlternativeSpecIndex(), 
									startNode.getExpectedElementSpecIndex(), 
									startNode.isExpectedElementSpecMatchedOnce(), 
									parsedElements);
							rootElements.addAll(assumeCompleted(parentNode, stopTokenIndex));
						}
					}
				}
			}
		} 
		return rootElements;
	}
	
}
