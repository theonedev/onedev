package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		Preconditions.checkArgument(!stream.isEof());
		
		List<TokenNode> paths = new ArrayList<>();
		boolean matched;
		int maxMatchDistance = 0;
		int index = stream.getIndex();
		parent = new Node(this, parent, previous);
		for (AlternativeSpec alternative: alternatives) {
			SpecMatch alternativeMatch = 
					alternative.match(stream, parent, parent, new HashMap<>(checkedIndexes));
			for (TokenNode path: alternativeMatch.getPaths()) {
				int matchDistance = path.getToken().getStopIndex();
				if (matchDistance > maxMatchDistance) {
					maxMatchDistance = matchDistance;
					paths.clear();
					paths.add(path);
				} else if (matchDistance == maxMatchDistance) {
					paths.add(path);
				} 
			}
			stream.setIndex(index);
		}

		if (!matches.isEmpty())
			stream.setIndex(stream.indexOf(matches.get(0).getToken()) + 1);
		
		return matches;
	}

	@Override
	public boolean match(AssistStream stream, Map<String, Integer> checkedIndexes) {
		for (AlternativeSpec alternative: alternatives) {
			if (alternative.match(stream, new HashMap<>(checkedIndexes)))
				return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "rule: " + name;
	}

}
