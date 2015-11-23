package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class AnyTokenElementSpec extends ElementSpec {

	public AnyTokenElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity) {
		super(codeAssist, label, multiplicity);
	}

	@Override
	public List<ElementSuggestion> doSuggestFirst(Node parent, String matchWith, TokenStream stream) {
		return new ArrayList<ElementSuggestion>();
	}

	@Override
	public boolean skipMandatories(TokenStream stream) {
		return false;
	}

	@Override
	public List<String> getMandatories() {
		return new ArrayList<>();
	}

	@Override
	protected boolean matchOnce(TokenStream stream) {
		if (!stream.isEof())
			stream.increaseIndex();
		return true;
	}

	@Override
	protected List<TokenNode> getPartialMatchesOnce(TokenStream stream, Node parent) {
		Preconditions.checkArgument(!stream.isEof());
		
		Token token = stream.getCurrentToken();
		stream.increaseIndex();
		return Lists.newArrayList(new TokenNode(this, parent, token));
	}

}
