package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

public class AnyTokenElementSpec extends ElementSpec {

	public AnyTokenElementSpec(CodeAssist codeAssist, String label, Multiplicity multiplicity) {
		super(codeAssist, label, multiplicity);
	}

	@Override
	protected boolean matchEmptyInElement() {
		return false;
	}

	@Override
	public List<ElementSuggestion> suggestFirst(Node parent, String matchWith) {
		return new ArrayList<ElementSuggestion>();
	}

	@Override
	public CaretMove moveCaretToEdit(TokenStream stream) {
		return new CaretMove(0, true);
	}

}
