package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

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
	public List<ElementSuggestion> suggestFirst(@Nullable Node parent, String matchWith, AssistStream stream) {
		parent = new Node(this, parent);
		List<ElementSuggestion> first = new ArrayList<>();
		for (AlternativeSpec alternative: alternatives)
			first.addAll(alternative.suggestFirst(parent, matchWith, stream));
		return first;
	}

	@Override
	public List<TokenNode> getPartialMatches(AssistStream stream, Node parent) {
		Preconditions.checkArgument(!stream.isEof());
		
		List<TokenNode> matches = new ArrayList<>();
		int maxMatchDistance = 0;
		int index = stream.getIndex();
		parent = new Node(this, parent, stream.getCurrentToken());
		for (AlternativeSpec alternative: alternatives) {
			List<TokenNode> alternativeMatches = alternative.getPartialMatches(stream, parent);
			for (TokenNode node: alternativeMatches) {
				int matchDistance = node.getStart().getStopIndex();
				if (matchDistance > maxMatchDistance) {
					maxMatchDistance = matchDistance;
					matches.clear();
					matches.add(node);
				} else if (matchDistance == maxMatchDistance) {
					matches.add(node);
				} 
			}
			stream.setIndex(index);
		}

		if (!matches.isEmpty())
			stream.setIndex(stream.indexOf(matches.get(0).getStart()) + 1);
		
		return matches;
	}

	@Override
	public boolean match(AssistStream stream) {
		for (AlternativeSpec alternative: alternatives) {
			if (alternative.match(stream))
				return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "rule: " + name;
	}

}
