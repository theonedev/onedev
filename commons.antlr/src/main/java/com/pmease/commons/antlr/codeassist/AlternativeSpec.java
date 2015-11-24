package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;

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
	public List<TokenNode> getPartialMatches(AssistStream stream, Node parent, Map<String, Integer> checkedIndexes) {
		Preconditions.checkArgument(!stream.isEof());
		
		parent = new Node(this, parent, stream.getCurrentToken());
		int index = stream.getIndex();
		List<TokenNode> matches = new ArrayList<>();
		for (ElementSpec elementSpec: elements) {
			List<TokenNode> elementMatches = elementSpec.getPartialMatches(stream, parent, new HashMap<>(checkedIndexes));
			if (elementMatches.isEmpty()) {
				if (!elementSpec.match(codeAssist.lex(""), new HashMap<String, Integer>())) {
					stream.setIndex(index);
					return elementMatches;
				}
			} else if (stream.isEof()) {
				return elementMatches;
			} else {
				matches = elementMatches;
			}
		}
		return matches;
	}

	@Override
	public List<ElementSuggestion> suggestFirst(Node parent, String matchWith, AssistStream stream, Set<String> checkedRules) {
		List<ElementSuggestion> first = new ArrayList<>();
		parent = new Node(this, parent);
		for (ElementSpec element: elements) {
			first.addAll(element.suggestFirst(parent, matchWith, stream, new HashSet<>(checkedRules)));
			if (!element.match(codeAssist.lex(""), new HashMap<String, Integer>()))
				break;
		}
		return first;
	}

	@Override
	public boolean match(AssistStream stream, Map<String, Integer> checkedIndexes) {
		int index = stream.getIndex();
		for (ElementSpec element: elements) {
			if (!element.match(stream, new HashMap<>(checkedIndexes))) {
				stream.setIndex(index);
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "alternative: " + elements;
	}
	
}
