package com.pmease.commons.antlr.codeassist;

import java.util.List;

import org.antlr.v4.runtime.Token;

public abstract class Spec {
	
	protected final CodeAssist codeAssist;

	public Spec(CodeAssist codeAssist) {
		this.codeAssist = codeAssist;
	}
	
	public abstract List<TokenNode> match(List<Token> tokens, int from);
	
	public abstract List<ElementSuggestion> suggestFirst(Node parent, String matchWith);
	
	public abstract boolean matches(TokenStream stream);

	public CodeAssist getCodeAssist() {
		return codeAssist;
	}
	
}
