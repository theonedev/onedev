package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class RuleSpec extends Spec {

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
	public List<ElementSuggestion> suggestFirst(@Nullable Node parent, String matchWith) {
		Node ruleNode =  new Node(this, parent);
		List<ElementSuggestion> first = new ArrayList<>();
		for (AlternativeSpec alternative: alternatives)
			first.addAll(alternative.suggestFirst(ruleNode, matchWith));
		return first;
	}

	@Override
	public List<TokenNode> getPartialMatches(TokenStream stream, Node parent) {
		List<TokenNode> partialMatches = null;
		int maxMatchDistance = 0;
		int index = stream.getIndex();
		parent = new Node(this, parent);
		for (AlternativeSpec alternative: alternatives) {
			List<TokenNode> alternativeMatches = alternative.getPartialMatches(stream, parent);
			if (alternativeMatches != null) {
				if (partialMatches == null)
					partialMatches = new ArrayList<>();
				for (TokenNode node: alternativeMatches) {
					int matchDistance = node.getStart().getStopIndex();
					if (matchDistance > maxMatchDistance) {
						maxMatchDistance = matchDistance;
						partialMatches.clear();
						partialMatches.add(node);
					} else if (matchDistance == maxMatchDistance) {
						partialMatches.add(node);
					} 
				}
			}
			stream.setIndex(index);
		}

		if (partialMatches != null && !partialMatches.isEmpty())
			stream.setIndex(stream.indexOf(partialMatches.get(0).getStart()) + 1);
		
		return partialMatches;
	}

	@Override
	public boolean match(TokenStream stream) {
		for (AlternativeSpec alternative: alternatives) {
			if (alternative.match(stream))
				return true;
		}
		return false;
	}

}
