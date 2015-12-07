package com.pmease.commons.antlr.codeassist;

import java.io.Serializable;
import java.util.ArrayList;
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
	public abstract List<TokenNode> match(AssistStream stream, Node parent, Node previous, 
			Map<String, Integer> checkedIndexes, boolean fullMatch);
	
	public abstract List<ElementSuggestion> suggestFirst(ParseTree parseTree, Node parent, 
			String matchWith, Set<String> checkedRules);
	
	public CodeAssist getCodeAssist() {
		return codeAssist;
	}
	
	protected List<TokenNode> initMatches(AssistStream stream, Node parent, Node previous) {
		List<TokenNode> matches = new ArrayList<>();
		matches.add(new TokenNode(null, parent, previous, new FakedToken(stream)));
		return matches;
	}
	
	public boolean matches(AssistStream stream) {
		for (TokenNode match: match(stream, null, null, new HashMap<String, Integer>(), true)) {
			if (match.getToken().getTokenIndex() == stream.size()-1)
				return true;
		}
		return false;
	}
	
	public boolean matches(String content) {
		AssistStream stream = codeAssist.lex(content);
		for (TokenNode match: match(stream, null, null, new HashMap<String, Integer>(), true)) {
			if (stream.isLastToken(match.getToken()))
				return true;
		}
		return false;
	}
	
	public abstract Set<Integer> getLeadingTokenTypes();
	
	public abstract boolean matchesEmpty();
	
}
