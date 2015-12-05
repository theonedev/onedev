package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RuleSpec extends Spec {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final List<AlternativeSpec> alternatives;

	public RuleSpec(CodeAssist codeAssist, String name, List<AlternativeSpec> alternatives) {
		super(codeAssist);
		
		this.name = name;
		this.alternatives = alternatives;
	}
	
	public String getName() {
		return name;
	}

	public List<AlternativeSpec> getAlternatives() {
		return alternatives;
	}

	@Override
	public List<ElementSuggestion> suggestFirst(ParseTree parseTree, Node parent, 
			String matchWith, Set<String> checkedRules) {
		parent = new Node(this, parent, null);
		List<ElementSuggestion> first = new ArrayList<>();
		for (AlternativeSpec alternative: alternatives)
			first.addAll(alternative.suggestFirst(parseTree, parent, matchWith, new HashSet<>(checkedRules)));
		return first;
	}

	@Override
	public List<TokenNode> match(AssistStream stream, Node parent, Node previous, 
			Map<String, Set<RuleRefContext>> ruleRefHistory, boolean fullMatch) {
		List<TokenNode> matches = new ArrayList<>();
		int index = stream.getIndex();
		parent = new Node(this, parent, previous);
		boolean fakedAdded = false;
		for (AlternativeSpec alternative: alternatives) {
			for (TokenNode match: alternative.match(stream, parent, parent, copy(ruleRefHistory), fullMatch)) {
				if (match.getToken().getTokenIndex() == index-1) {
					if (!fakedAdded) {
						matches.add(new TokenNode(null, parent, parent, new FakedToken(index-1)));
						fakedAdded = true;
					}
				} else {
					matches.add(match);
				}
			}
			stream.setIndex(index);
		}
		codeAssist.prune(matches, stream);
		return matches;
	}

	@Override
	public String toString() {
		return "name: " + name + ", alternatives: " + alternatives;
	}

}
