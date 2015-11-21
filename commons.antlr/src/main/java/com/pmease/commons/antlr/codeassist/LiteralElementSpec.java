package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

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
	public List<ElementSuggestion> suggestFirst(Node parent, String matchWith) {
		return Lists.newArrayList(new ElementSuggestion(new Node(this, parent), new ArrayList<CaretAwareText>()));
	}

	@Override
	public boolean skipMandatories(TokenStream stream) {
		if (stream.isEnd()) {
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
		if (stream.isEnd()) {
			return false;
		} else if (stream.getCurrentToken().getType() == type) {
			stream.increaseIndex();
			return true;
		} else {
			return false;
		}
	}

}
