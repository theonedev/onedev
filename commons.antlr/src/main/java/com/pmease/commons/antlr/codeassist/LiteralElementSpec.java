package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class LiteralElementSpec extends TokenElementSpec {

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
	public List<ElementSuggestion> doSuggestFirst(Node parent, String matchWith, TokenStream stream) {
		if (!matchWith.equals(literal) && literal.toLowerCase().startsWith(matchWith.toLowerCase())) {
			CaretAwareText text = new CaretAwareText(literal, literal.length());
			return Lists.newArrayList(new ElementSuggestion(new Node(this, parent), Lists.newArrayList(text)));
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public boolean skipMandatories(TokenStream stream) {
		if (stream.isEof()) {
			return false;
		} else if (stream.getCurrentToken().getType() == type) {
			stream.increaseIndex();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<String> getMandatories() {
		return Lists.newArrayList(literal);
	}

	@Override
	protected boolean matchOnce(TokenStream stream) {
		if (stream.isEof()) {
			return false;
		} else if (stream.getCurrentToken().getType() == type) {
			stream.increaseIndex();
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected List<TokenNode> getPartialMatchesOnce(TokenStream stream, Node parent) {
		Preconditions.checkArgument(!stream.isEof());
		
		Token token = stream.getCurrentToken();
		if (token.getType() == type) {
			stream.increaseIndex();
			return Lists.newArrayList(new TokenNode(this, parent, token));
		} else {
			return new ArrayList<>();
		}
	}

}
