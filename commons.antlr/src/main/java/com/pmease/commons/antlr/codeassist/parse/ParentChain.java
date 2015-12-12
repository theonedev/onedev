package com.pmease.commons.antlr.codeassist.parse;

import java.util.List;

import javax.annotation.Nullable;

public class ParentChain {
	
	private final List<ParsedElement> parents;
	
	public ParentChain(List<ParsedElement> parents) {
		this.parents = parents;
	}

	public List<ParsedElement> getParents() {
		return parents;
	}
	
	@Nullable
	public ParsedElement findParentByLabel(ParsedElement parsedElement, String label) {
		for (ParsedElement parent: parents) {
			if (label.equals(parent.getLabel()))
				return parent;
		}
		return null;
	}

	@Nullable
	public ParsedElement findParentByRule(ParsedElement parsedElement, String ruleName) {
		for (ParsedElement parent: parents) {
			if (ruleName.equals(parent.getNode().getRuleSpec().getName()))
				return parent;
		}
		return null;
	}

	@Nullable
	public ParsedElement getRoot() {
		return parents.get(parents.size()-1);
	}

	@Nullable
	public ParsedElement getParent(int level) {
		if (level < parents.size())
			return parents.get(level);
		else
			return null;
	}
	
	@Nullable
	public ParsedElement getParent() {
		return getParent(0);
	}
	
}
