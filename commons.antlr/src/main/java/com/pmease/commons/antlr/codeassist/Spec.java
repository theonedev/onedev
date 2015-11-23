package com.pmease.commons.antlr.codeassist;

import java.util.List;

public abstract class Spec {
	
	protected final CodeAssist codeAssist;

	public Spec(CodeAssist codeAssist) {
		this.codeAssist = codeAssist;
	}

	/**
	 * @param stream
	 * @return
	 * 			empty if the rule does not match part or all of the stream
	 */
	public abstract List<TokenNode> getPartialMatches(TokenStream stream, Node parent);
	
	public abstract List<ElementSuggestion> suggestFirst(Node parent, String matchWith, TokenStream stream);
	
	public abstract boolean match(TokenStream stream);
	
	public CodeAssist getCodeAssist() {
		return codeAssist;
	}
	
}
