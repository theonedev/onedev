package com.pmease.commons.antlr.grammarspec;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;

import com.pmease.commons.antlr.parsetree.Node;
import com.pmease.commons.antlr.parsetree.TokenNode;

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
	public List<TokenNode> getFirst(Node parent) {
		List<TokenNode> first = new ArrayList<>();
		Node alternativeNode = new Node(this, parent);
		for (ElementSpec element: elements) {
			first.addAll(element.getFirst(alternativeNode));
			if (!element.matchEmpty())
				break;
		}
		return first;
	}

	@Override
	public boolean matchEmpty() {
		for (ElementSpec element: elements) {
			if (!element.matchEmpty())
				return false;
		}
		return true;
	}
	
}
