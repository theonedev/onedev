package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.Token;

public class NotTokenElementSpec extends ElementSpec {

	private final Set<Integer> notTokenTypes;
	
	public NotTokenElementSpec(CodeAssist codeAssist, String label, 
			Multiplicity multiplicity, Set<Integer> notTokenTypes) {
		super(codeAssist, label, multiplicity);
		
		this.notTokenTypes = notTokenTypes;
	}

	public Set<Integer> getNotTokenTypes() {
		return notTokenTypes;
	}

	@Override
	public List<ElementSuggestion> suggestFirst(Node parent, String matchWith) {
		return new ArrayList<>();
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
	protected boolean matchesOnce(TokenStream stream) {
		if (stream.isEnd()) {
			return !notTokenTypes.contains(Token.EOF);
		} else {
			Token token = stream.getCurrentToken();
			stream.increaseIndex();
			return !notTokenTypes.contains(token.getType());
		}
	}

}
