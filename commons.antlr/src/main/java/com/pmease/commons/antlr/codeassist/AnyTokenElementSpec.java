package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

public class AnyTokenElementSpec extends ElementSpec {

	public AnyTokenElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity) {
		super(codeAssist, label, multiplicity);
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
		if (!stream.isEnd())
			stream.increaseIndex();
		return true;
	}

}
