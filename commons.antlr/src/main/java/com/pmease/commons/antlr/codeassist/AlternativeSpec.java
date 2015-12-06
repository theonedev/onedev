package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
			Map<String, Integer> checkedIndexes, boolean fullMatch) {
		parent = new Node(this, parent, previous);
		List<TokenNode> stopMatches = new ArrayList<>();
		List<TokenNode> matches = initMatches(stream, parent, parent);
		if (!stream.isEof() || fullMatch) {
			for (ElementSpec elementSpec: elements) {
				matches = elementSpec.match(matches, stream, parent, new HashMap<>(checkedIndexes), fullMatch);
				if (!fullMatch) {
					for (Iterator<TokenNode> it = matches.iterator(); it.hasNext();) {
						TokenNode match = it.next();
						if (match.getToken().getTokenIndex() == stream.size()-1) {
							stopMatches.add(match);
							it.remove();
						}
					}
				}
				codeAssist.prune(matches, stream);
				if (matches.isEmpty())
					break;
			}
		}
		matches.addAll(stopMatches);
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
			if (!element.matchesEmpty())
				break;
		}
		return first;
	}

	@Override
	public String toString() {
		return "elements: " + elements;
	}

	@Override
	public Set<Integer> getMandatoryTokenTypes(Set<String> checkedRules) {
		for (ElementSpec elementSpec: elements) {
			Set<Integer> tokenTypes = elementSpec.getMandatoryTokenTypes(new HashSet<>(checkedRules));
			if (!tokenTypes.isEmpty())
				return tokenTypes;
		}
		return new HashSet<>();
	}

}
