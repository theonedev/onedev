package com.pmease.commons.antlr.codeassist;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Spec implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected final CodeAssist codeAssist;

	public Spec(CodeAssist codeAssist) {
		this.codeAssist = codeAssist;
	}

	public boolean matches(AssistStream stream) {
		return match(stream, null, null, new HashMap<String, Integer>()).isMatched() && stream.isEof();
	}
	
	public abstract SpecMatch match(AssistStream stream, 
			Node parent, Node previous, Map<String, Integer> checkedIndexes);
	
	public abstract List<ElementSuggestion> suggestFirst(ParseTree parseTree, Node parent, 
			String matchWith, Set<String> checkedRules);
	
	public CodeAssist getCodeAssist() {
		return codeAssist;
	}
	
}
