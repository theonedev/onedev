package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;

import com.google.common.collect.Lists;

public class EofElementSpec extends TokenElementSpec {

	public EofElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity) {
		super(codeAssist, label, multiplicity, Token.EOF);
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
		return stream.isEof();
	}

	@Override
	protected List<TokenNode> getPartialMatchesOnce(TokenStream stream, Node parent) {
		if (stream.isEof())
			return Lists.newArrayList(new TokenNode(this, parent, stream.getCurrentToken()));
		else
			return null;
	}

}
