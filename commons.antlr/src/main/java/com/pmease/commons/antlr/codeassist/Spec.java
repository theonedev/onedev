package com.pmease.commons.antlr.codeassist;

import java.io.Serializable;
import java.util.List;

public abstract class Spec implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected final CodeAssist codeAssist;

	public Spec(CodeAssist codeAssist) {
		this.codeAssist = codeAssist;
	}

	/**
	 * @param stream
	 * @return
	 * 			empty if the rule does not match part or all of the stream
	 */
	public abstract List<TokenNode> getPartialMatches(AssistStream stream, Node parent);
	
	public abstract List<ElementSuggestion> suggestFirst(Node parent, String matchWith, AssistStream stream);
	
	public abstract boolean match(AssistStream stream);
	
	public CodeAssist getCodeAssist() {
		return codeAssist;
	}
	
}
