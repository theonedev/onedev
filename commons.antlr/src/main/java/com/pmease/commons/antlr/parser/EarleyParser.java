package com.pmease.commons.antlr.parser;

import java.util.ArrayList;
import java.util.Collection;
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

/**
 * A Earley parser (https://en.wikipedia.org/wiki/Earley_parser) to parse user inputs. 
 * It is very suited for code assistance purpose as it can provide partial 
 * parse results and tell us what terminals are expected next. Note that for large 
 * inputs (thousands of lines for example), our earley implementation performs bad 
 * and consumes a lot of memory, but it serves our purpose to provide a mini code 
 * assistance for web input 
 * 
 * @author robin
 *
 */
public class EarleyParser {

	private final RuleSpec rule;
	
	private final List<Token> tokens;
	
	private int tokenIndex = 0;
	
	private final List<Chart> charts = new ArrayList<>();

	public EarleyParser(RuleSpec rule, List<Token> tokens) {
		this.rule = rule;
		this.tokens = tokens;
		
		Set<State> states = new HashSet<>();
		for (int i=0; i<rule.getAlternatives().size(); i++) 
			states.add(new State(tokenIndex, rule, i, 0, false, new ArrayList<Element>()));

		while (!states.isEmpty()) {
			Chart chart = new Chart(this, tokenIndex, states);
			charts.add(chart);
			for (State state: Lists.newArrayList(chart.getStates())) // avoid concurrent modification
				process(state, chart);

			if (tokenIndex == tokens.size())
				break;
			
			states = new HashSet<>();
			for (State state: chart.getStates()) 
				scan(state, states);
			tokenIndex++;
		}
	}
	
	private void process(State state, Chart chart) {
		if (!state.isCompleted()) { // predict
			ElementSpec expectedElementSpec = state.getExpectedElementSpec();
			if (expectedElementSpec instanceof RuleRefElementSpec) {
				RuleRefElementSpec ruleRefElement = (RuleRefElementSpec) expectedElementSpec;
				RuleSpec elementRule = ruleRefElement.getRule();
				for (int i=0; i<elementRule.getAlternatives().size(); i++) {
					State predictedState = new State(tokenIndex, 
							elementRule, i, 0, false, new ArrayList<Element>());
					if (chart.getStates().add(predictedState))
						process(predictedState, chart);
				}
			}
			if (expectedElementSpec.isOptional() || state.isExpectedElementSpecMatchedOnce()) {
				State advancedState = new State(state.getOriginPosition(), state.getRuleSpec(), 
						state.getAlternativeSpecIndex(), state.getExpectedElementSpecIndex()+1, false, 
						new ArrayList<>(state.getElements()));
				if (chart.getStates().add(advancedState))
					process(advancedState, chart);
			}
		} else { // complete
			Chart startChart = charts.get(state.getOriginPosition());
			Collection<State> startStates;
			if (state.getOriginPosition() == chart.getPosition())
				startStates = Lists.newArrayList(startChart.getStates()); // avoid concurrent modification
			else
				startStates = startChart.getStates();
			for (State startState: startStates) {
				if (!startState.isCompleted()) {
					ElementSpec expectedElementSpec = startState.getExpectedElementSpec();
					if (expectedElementSpec instanceof RuleRefElementSpec) {
						RuleRefElementSpec ruleRefElement = (RuleRefElementSpec) expectedElementSpec;
						if (ruleRefElement.getRuleName().equals(state.getRuleSpec().getName())) {
							State advancedState;
							List<Element> elements = new ArrayList<>(startState.getElements());
							if (!state.getElements().isEmpty())
								elements.add(new Element(this, ruleRefElement, chart.getPosition(), state));
							if (!expectedElementSpec.isMultiple()) {
								advancedState = new State(startState.getOriginPosition(), 
										startState.getRuleSpec(), startState.getAlternativeSpecIndex(), 
										startState.getExpectedElementSpecIndex()+1, false, elements);
							} else {
								advancedState = new State(startState.getOriginPosition(), 
										startState.getRuleSpec(), startState.getAlternativeSpecIndex(), 
										startState.getExpectedElementSpecIndex(), true, elements);
							}
							if (chart.getStates().add(advancedState))
								process(advancedState, chart);
						}
					}
				}
			}
		}
	}
	
	private void scan(State state, Set<State> nextStates) {
		if (!state.isCompleted()) {
			ElementSpec expectedElementSpec = state.getExpectedElementSpec();
			int tokenType = tokens.get(tokenIndex).getType();
			if ((expectedElementSpec instanceof TerminalElementSpec) 
					&& ((TerminalElementSpec)expectedElementSpec).isToken(tokenType)) {
				State scannedState;
				List<Element> elements = new ArrayList<>(state.getElements());
				elements.add(new Element(this, expectedElementSpec, tokenIndex+1, null));
				if (!expectedElementSpec.isMultiple()) {
					scannedState = new State(state.getOriginPosition(), state.getRuleSpec(), 
							state.getAlternativeSpecIndex(), state.getExpectedElementSpecIndex()+1, 
							false, elements);
				} else {
					scannedState = new State(state.getOriginPosition(), state.getRuleSpec(), 
							state.getAlternativeSpecIndex(), state.getExpectedElementSpecIndex(), 
							true, elements);
				}
				nextStates.add(scannedState);
			}
		}
	}
	
	public List<Chart> getCharts() {
		return charts;
	}
	
	public RuleSpec getRule() {
		return rule;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public List<State> getMatches() {
		if (charts.size() == tokens.size()+1) 
			return charts.get(tokens.size()).getMatches();
		else
			return new ArrayList<>();
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
	
	/**
	 * Get the next token index after the match. 
	 * 
	 * @param tokens
	 * 			tokens to match against
	 * @return
	 * 			next token index after the match.
	 * 			<ul><li>greater than 0 if the rule matches part of the tokens
	 * 			<li>0 if the rule does not match any tokens, and the rule allows to be empty
	 * 			<li>-1 if the rule does not match any tokens, and the rule does not allow 
	 * 			to be empty
	 */
	public int getEndOfMatch() {
		for (int i=charts.size()-1; i>=0; i--) {
			Chart state = charts.get(i);
			if (!state.getMatches().isEmpty())
				return i;
		}
		return -1;
	}
	
}
