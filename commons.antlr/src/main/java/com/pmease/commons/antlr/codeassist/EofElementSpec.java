package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;

import com.google.common.base.Preconditions;

public class EofElementSpec extends TokenElementSpec {

	public EofElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity) {
		super(codeAssist, label, multiplicity, Token.EOF);
	}

	@Override
	public List<ElementSuggestion> suggestFirst(Node parent, String matchWith) {
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
		return stream.isEnd();
	}

	@Override
	protected List<TokenNode> getPartialMatchesOnce(TokenStream stream, Node parent) {
		Preconditions.checkArgument(!stream.isEnd());
		return null;
	}

}
