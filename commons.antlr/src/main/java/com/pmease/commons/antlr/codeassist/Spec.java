package com.pmease.commons.antlr.codeassist;

import java.util.List;

import javax.annotation.Nullable;

public abstract class Spec {
	
	protected final CodeAssist codeAssist;

	public Spec(CodeAssist codeAssist) {
		this.codeAssist = codeAssist;
	}

	/**
	 * @param stream
	 * @return
	 * 			<tt>null</tt> if the rule does not match part or full of the stream
	 */
	@Nullable
	public abstract List<TokenNode> getPartialMatches(TokenStream stream, Node parent);
	
	public abstract List<ElementSuggestion> suggestFirst(Node parent, String matchWith, TokenStream stream);
	
	public abstract boolean match(TokenStream stream);

	public CodeAssist getCodeAssist() {
		return codeAssist;
	}
	
}
