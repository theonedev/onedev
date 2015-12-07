package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

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
		if (!checkedRules.contains(name)) {
			checkedRules.add(name);
			for (AlternativeSpec alternative: alternatives)
				first.addAll(alternative.suggestFirst(parseTree, parent, matchWith, new HashSet<>(checkedRules)));
		} 
		
		return first;
	}

	@Override
	public List<TokenNode> match(AssistStream stream, Node parent, Node previous, 
			Map<String, Integer> checkedIndexes, boolean fullMatch) {
		List<TokenNode> matches = new ArrayList<>();
		int index = stream.getIndex();
		Integer checkedIndex = checkedIndexes.get(name);
		if (checkedIndex == null || index != checkedIndex) {
			checkedIndexes.put(name, index);
			parent = new Node(this, parent, previous);
			boolean fakedAdded = false;
			for (AlternativeSpec alternative: alternatives) {
				for (TokenNode match: alternative.match(stream, parent, parent, new HashMap<>(checkedIndexes), fullMatch)) {
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
		} else {
			throw new RuntimeException("Direct or indirect left recursion rule detected: " + name 
					+ ", please refactor to remove it.");
		}
		codeAssist.prune(matches, stream);
		return matches;
	}

	@Override
	public String toString() {
		List<String> alternativeStrings = new ArrayList<>();
		for (AlternativeSpec alternative: alternatives)
			alternativeStrings.add(alternative.toString());
		return StringUtils.join(alternativeStrings, " | ");
	}

}
