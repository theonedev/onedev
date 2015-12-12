package com.pmease.commons.antlr.codeassist.parse;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.Token;

import com.pmease.commons.antlr.codeassist.ElementSpec;

public class ParsedElement {
	
	private final ElementSpec spec;
	
	private final int nextTokenIndex;
	
	private final ParseNode node;
	
	public ParsedElement(@Nullable ElementSpec spec, int nextTokenIndex, @Nullable ParseNode node) {
		this.spec = spec;
		this.nextTokenIndex = nextTokenIndex;
		this.node = node;
	}

	@Nullable
	public ElementSpec getSpec() {
		return spec;
	}
	
	public boolean isRoot() {
		return spec == null;
	}
	
	public boolean isTerminal() {
		return node == null;
	}
	
	@Nullable
	public String getLabel() {
		return spec!=null?spec.getLabel():null;
	}

	public int getNextTokenIndex() {
		return nextTokenIndex;
	}

	@Nullable
	public ParseNode getNode() {
		return node;
	}
	
	public List<Token> getParsedTokens(List<Token> tokens) {
		if (node != null)
			return tokens.subList(node.getStartTokenIndex(), nextTokenIndex);
		else
			return tokens.subList(nextTokenIndex, nextTokenIndex+1);
	}
	
	public List<ParsedElement> getChildrenByLabel(String label, boolean recursive) {
		List<ParsedElement> children = new ArrayList<>();
		if (node != null) {
			for (ParsedElement child: node.getParsedElements()) {
				if (label.equals(child.getLabel()))
					children.add(child);
				if (recursive)
					children.addAll(child.getChildrenByLabel(label, recursive));
			}
		}
		return children;
	}
	
	public List<ParsedElement> getChildrenByRule(String ruleName, boolean recursive) {
		List<ParsedElement> children = new ArrayList<>();
		if (node != null) {
			for (ParsedElement child: node.getParsedElements()) {
				if (ruleName.equals(child.getNode().getRuleSpec().getName()))
					children.add(child);
				if (recursive)
					children.addAll(child.getChildrenByRule(ruleName, recursive));
			}
		}
		return children;
	}
	
}
