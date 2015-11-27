package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.HashMap;
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
	public SpecMatch match(AssistStream stream, 
			Node parent, Node previous, Map<String, Integer> checkedIndexes) {
		List<TokenNode> matchedPaths = new ArrayList<>();
		List<TokenNode> unmatchedPaths = new ArrayList<>();
		boolean matched = false;
		int maxMatchIndex = -1;
		int maxUnmatchIndex = -1;
		int index = stream.getIndex();
		parent = new Node(this, parent, previous);
		for (AlternativeSpec alternative: alternatives) {
			SpecMatch match = alternative.match(stream, parent, parent, new HashMap<>(checkedIndexes));
			if (match.isMatched()) {
				if (stream.getIndex() > maxMatchIndex) {
					maxMatchIndex = stream.getIndex();
					matchedPaths = match.getPaths();
				} else if (stream.getIndex() == maxMatchIndex) {
					matchedPaths.addAll(match.getPaths());
				}
				matched = true;
			} else {
				if (stream.getIndex() > maxUnmatchIndex) {
					maxUnmatchIndex = stream.getIndex();
					unmatchedPaths = match.getPaths();
				} else if (stream.getIndex() == maxUnmatchIndex) {
					unmatchedPaths.addAll(match.getPaths());
				}
			}
			stream.setIndex(index);
		}

		stream.setIndex(matched?maxMatchIndex:maxUnmatchIndex);
		return new SpecMatch(matched?matchedPaths:unmatchedPaths, matched);
	}

	@Override
	public String toString() {
		return "rule: " + name;
	}

}
