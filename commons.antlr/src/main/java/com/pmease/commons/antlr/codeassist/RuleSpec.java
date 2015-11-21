package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

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
	public List<TokenNode> getPartialMatches(TokenStream stream) {
		Preconditions.checkArgument(!stream.isEnd());
		List<TokenNode> partialMatches = new ArrayList<>();
		int maxMatchDistance = 0;
		int index = stream.getIndex();
		for (AlternativeSpec alternative: alternatives) {
			for (TokenNode node: alternative.getPartialMatches(stream)) {
				int matchDistance = node.getStart().getStopIndex();
				if (matchDistance > maxMatchDistance) {
					maxMatchDistance = matchDistance;
					partialMatches.clear();
					partialMatches.add(node);
				} else if (matchDistance == maxMatchDistance) {
					partialMatches.add(node);
				} 
			}
			stream.setIndex(index);
		}

		if (!partialMatches.isEmpty())
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
