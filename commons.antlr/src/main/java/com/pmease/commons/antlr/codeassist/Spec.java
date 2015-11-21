package com.pmease.commons.antlr.codeassist;

import java.util.List;

public abstract class Spec {
	
	protected final CodeAssist codeAssist;

	public Spec(CodeAssist codeAssist) {
		this.codeAssist = codeAssist;
	}
	
	public abstract List<TokenNode> getPartialMatches(TokenStream stream);
	
	public abstract List<ElementSuggestion> suggestFirst(Node parent, String matchWith);
	
	public abstract boolean match(TokenStream stream);

	public CodeAssist getCodeAssist() {
		return codeAssist;
	}
	
}
