package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AlternativeSpec extends Spec {

	private static final long serialVersionUID = 1L;

	private final String label;
	
	private final List<ElementSpec> elements;
	
	public AlternativeSpec(CodeAssist codeAssist, String label, List<ElementSpec> elements) {
		super(codeAssist);
		
		this.label = label;
		this.elements = elements;
	}

	public String getLabel() {
		return label;
	}

	public List<ElementSpec> getElements() {
		return elements;
	}
	
	@Override
	public List<TokenNode> match(AssistStream stream, Node parent, Node previous, 
			Map<String, Set<RuleRefContext>> ruleRefHistory, boolean fullMatch) {
		parent = new Node(this, parent, previous);
		List<TokenNode> matches = initMatches(stream, parent, parent);
		for (ElementSpec elementSpec: elements) {
			matches = elementSpec.match(matches, stream, parent, copy(ruleRefHistory), fullMatch);
			if (matches.isEmpty())
				break;
		}
		return matches;
	}

	@Override
	public List<ElementSuggestion> suggestFirst(ParseTree parseTree, Node parent, 
			String matchWith, Set<String> checkedRules) {
		List<ElementSuggestion> first = new ArrayList<>();
		parent = new Node(this, parent, null);
		for (ElementSpec element: elements) {
			first.addAll(element.suggestFirst(parseTree, parent, matchWith, new HashSet<>(checkedRules)));
			
			// consider next element if current element is optional
			if (!element.isOptional())
				break;
		}
		return first;
	}

	@Override
	public String toString() {
		return "elements: " + elements;
	}

}
