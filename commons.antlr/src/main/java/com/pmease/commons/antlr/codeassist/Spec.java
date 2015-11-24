package com.pmease.commons.antlr.codeassist;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	public abstract List<TokenNode> getPartialMatches(AssistStream stream, Node parent, Map<String, Integer> checkedIndexes);
	
	public abstract List<ElementSuggestion> suggestFirst(Node parent, String matchWith, AssistStream stream, Set<String> checkedRules);
	
	public abstract boolean match(AssistStream stream, Map<String, Integer> checkedIndexes);
	
	public CodeAssist getCodeAssist() {
		return codeAssist;
	}
	
}
