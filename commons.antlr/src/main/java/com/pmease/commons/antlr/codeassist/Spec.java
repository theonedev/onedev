package com.pmease.commons.antlr.codeassist;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

public abstract class Spec implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected final CodeAssist codeAssist;

	public Spec(CodeAssist codeAssist) {
		this.codeAssist = codeAssist;
	}

	/**
	 * Whether or there is an exact match between the spec and the the stream
	 *  
	 * @param stream
	 * 			stream to be matched
	 * @return
	 * 			true if there is an exact match between spec and stream
	 */
	public boolean matches(AssistStream stream) {
		for (TokenNode match: match(stream, null, null, new HashMap<String, Set<RuleRefContext>>()).keySet()) {
			if (match.getToken() == AssistStream.EOF)
				return true;
		}
		return false;
	}
	
	/**
	 * Match current spec against the stream. 
	 * 
	 * @param stream
	 * 			stream to match the spec against
	 * @param parent
	 * 			parent node of the newly created node to form parse tree hierarchy
	 * @param previous
	 * 			previous node of newly created node to form all nodes in certain parse tree
	 * @param checkedIndexes
	 * 			checked indexes to avoid infinite loop
	 * @return
	 * 			match result representing common paths between the spec and the stream. Note 
	 * 			that we can have match paths even if the whole spec is not matched, and in 
	 * 			that case, the paths tells to which point the match goes to 
	 */
	@Nullable
	public abstract Map<TokenNode, Integer> match(AssistStream stream, Node parent, Node previous, 
			Map<String, Set<RuleRefContext>> ruleRefContexts);
	
	public abstract List<ElementSuggestion> suggestFirst(ParseTree parseTree, Node parent, 
			String matchWith, Set<String> checkedRules);
	
	public CodeAssist getCodeAssist() {
		return codeAssist;
	}
	
	protected Map<TokenNode, Integer> initMatches(AssistStream stream, Node parent, Node previous) {
		Map<TokenNode, Integer> matches = new LinkedHashMap<>();
		matches.put(new TokenNode(null, parent, previous, AssistStream.SOF), stream.getIndex());
		return matches;
	}
	
	protected Map<String, Set<RuleRefContext>> copy(Map<String, Set<RuleRefContext>> ruleRefHistory) {
		Map<String, Set<RuleRefContext>> copy = new HashMap<>();
		for (Map.Entry<String, Set<RuleRefContext>> entry: ruleRefHistory.entrySet())
			copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
		return copy;
	}
	
}
