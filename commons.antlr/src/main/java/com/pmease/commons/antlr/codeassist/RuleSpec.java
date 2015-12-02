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
			Map<String, Set<RuleRefContext>> ruleRefHistory) {
		List<TokenNode> paths = null;
		int maxIndex = stream.getIndex();
		
		int index = stream.getIndex();
		parent = new Node(this, parent, previous);
		
		for (AlternativeSpec alternative: alternatives) {
			List<TokenNode> alternativePaths = alternative.match(stream, parent, parent, copy(ruleRefHistory));
			if (stream.getIndex() > maxIndex) {
				maxIndex = stream.getIndex();
				paths = alternativePaths;
			} else if (stream.getIndex() == maxIndex && alternativePaths != null) {
				if (paths == null)
					paths = alternativePaths;
				else
					paths.addAll(alternativePaths);
			}
			stream.setIndex(index);
		}

		stream.setIndex(maxIndex);
		
		return paths;
	}

	@Override
	public String toString() {
		return "name: " + name + ", alternatives: " + alternatives;
	}

}
