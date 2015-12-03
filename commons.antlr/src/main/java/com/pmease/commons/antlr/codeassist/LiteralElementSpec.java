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
	public List<ElementSuggestion> doSuggestFirst(ParseTree parseTree, Node parent, 
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
	public List<TokenNode> matchOnce(AssistStream stream, Node parent, Node previous, 
			Map<String, Set<RuleRefContext>> ruleRefHistory) {
		if (!stream.isEof()) {
			Token token = stream.getCurrentToken();
			if (token.getType() == type) {
				stream.increaseIndex();
				TokenNode tokenNode = new TokenNode(this, parent, previous, token);
				return Lists.newArrayList(tokenNode);
			} 
		}
		return null;
	}

	@Override
	protected String asString() {
		return "literal: '" + literal + "'";
	}
	
}
