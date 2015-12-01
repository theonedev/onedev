package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.HashMap;
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
	public SpecMatch match(AssistStream stream, Node parent, Node previous, 
			Map<String, Integer> checkedIndexes) {
		parent = new Node(this, parent, previous);
		List<TokenNode> paths = new ArrayList<>();
		for (ElementSpec elementSpec: elements) {
			if (!paths.isEmpty())
				previous = paths.get(paths.size()-1);
			else
				previous = parent;
			SpecMatch elementMatch = elementSpec.match(stream, parent, previous, 
					new HashMap<>(checkedIndexes));
			
			if (!elementMatch.getPaths().isEmpty())
				paths = elementMatch.getPaths();
			if (!elementMatch.isMatched())
				return new SpecMatch(paths, false);
		}
		return new SpecMatch(paths, true);
	}

	@Override
	public List<ElementSuggestion> suggestFirst(ParseTree parseTree, Node parent, 
			String matchWith, Set<String> checkedRules) {
		List<ElementSuggestion> first = new ArrayList<>();
		parent = new Node(this, parent, null);
		for (ElementSpec element: elements) {
			first.addAll(element.suggestFirst(parseTree, parent, matchWith, new HashSet<>(checkedRules)));
			
			// consider next element if current element is optional
			if (!element.matches(codeAssist.lex("")))
				break;
		}
		return first;
	}

	@Override
	public String toString() {
		return "alternative: " + elements;
	}
	
}
