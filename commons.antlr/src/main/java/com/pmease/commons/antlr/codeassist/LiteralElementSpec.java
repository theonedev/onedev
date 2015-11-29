package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.Token;

import com.google.common.collect.Lists;

public class LiteralElementSpec extends TokenElementSpec {

	private static final long serialVersionUID = 1L;
	
	private final String literal;
	
	public LiteralElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity, 
			int tokenType, String literal) {
		super(codeAssist, label, multiplicity, tokenType);
		
		this.literal = literal;
	}

	public String getLiteral() {
		return literal;
	}

	@Override
	public List<ElementSuggestion> doSuggestFirst(Node parent, ParseTree parseTree, 
			String matchWith, Set<String> checkedRules) {
		if (literal.toLowerCase().startsWith(matchWith.toLowerCase())) {
			InputSuggestion text = new InputSuggestion(literal);
			return Lists.newArrayList(new ElementSuggestion(parseTree, new Node(this, parent, null), 
					matchWith, Lists.newArrayList(text)));
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public MandatoryScan scanMandatories(Set<String> checkedRules) {
		return new MandatoryScan(Lists.newArrayList(literal), false);
	}

	@Override
	protected SpecMatch matchOnce(AssistStream stream, Node parent, 
			Node previous, Map<String, Integer> checkedIndexes) {
		Token token = stream.getCurrentToken();
		if (token.getType() == type) {
			stream.increaseIndex();
			TokenNode tokenNode = new TokenNode(this, parent, previous, token);
			return new SpecMatch(Lists.newArrayList(tokenNode), true);
		} else {
			return new SpecMatch(new ArrayList<TokenNode>(), false);
		}
	}

	@Override
	public String toString() {
		return "literal: '" + literal + "'";
	}
	
}
