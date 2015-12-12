package com.pmease.commons.antlr.codeassist.parse;

import javax.annotation.Nullable;

public class ParentedElement {

	private final ParentedElement parent;
	
	private final ParsedElement element;
	
	public ParentedElement(@Nullable ParentedElement parent, ParsedElement element) {
		this.parent = parent;
		this.element = element;
	}

	public ParentedElement getParent() {
		return parent;
	}

	public ParsedElement getElement() {
		return element;
	}

	@Nullable
	public ParentedElement findParentByLabel(String label) {
		ParentedElement current = parent;
		while (current != null) {
			if (label.equals(current.element.getLabel()))
				return current;
			current = current.parent;
		}
		return null;
	}

	@Nullable
	public ParentedElement findParentByRule(String ruleName) {
		ParentedElement current = parent;
		while (current != null) {
			if (ruleName.equals(current.element.getNode().getRuleSpec().getName()))
				return current;
			current = current.parent;
		}
		return null;
	}
	
	public ParentedElement getRoot() {
		if (parent == null)
			return this;
		else
			return parent.getRoot();
	}
	
}
