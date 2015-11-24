package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

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
	public List<TokenNode> getPartialMatches(AssistStream stream, Node parent) {
		Preconditions.checkArgument(!stream.isEof());
		
		parent = new Node(this, parent, stream.getCurrentToken());
		int index = stream.getIndex();
		List<TokenNode> matches = new ArrayList<>();
		for (ElementSpec elementSpec: elements) {
			List<TokenNode> elementMatches = elementSpec.getPartialMatches(stream, parent);
			if (elementMatches.isEmpty()) {
				if (!elementSpec.match(codeAssist.lex(""))) {
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
	public List<ElementSuggestion> suggestFirst(Node parent, String matchWith, AssistStream stream) {
		List<ElementSuggestion> first = new ArrayList<>();
		parent = new Node(this, parent);
		for (ElementSpec element: elements) {
			first.addAll(element.suggestFirst(parent, matchWith, stream));
			if (!element.match(codeAssist.lex("")))
				break;
		}
		return first;
	}

	@Override
	public boolean match(AssistStream stream) {
		int index = stream.getIndex();
		for (ElementSpec element: elements) {
			if (!element.match(stream)) {
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
