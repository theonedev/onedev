package com.pmease.commons.antlr.codeassist;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;

public class AlternativeSpec extends Spec {

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
	public List<TokenNode> match(List<Token> tokens, int from) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ElementSuggestion> suggestFirst(Node parent, String matchWith) {
		List<ElementSuggestion> first = new ArrayList<>();
		Node alternativeNode = new Node(this, parent);
		for (ElementSpec element: elements) {
			first.addAll(element.suggestFirst(alternativeNode, matchWith));
			if (!element.matches(new TokenStream(new ArrayList<Token>())))
				break;
		}
		return first;
	}

	@Override
	public boolean matches(TokenStream stream) {
		int index = stream.getIndex();
		for (ElementSpec element: elements) {
			if (!element.matches(stream)) {
				stream.setIndex(index);
				return false;
			}
		}
		return true;
	}
	
}
