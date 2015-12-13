package com.pmease.commons.antlr.codeassist;

import javax.annotation.Nullable;

import com.pmease.commons.antlr.parser.Element;

public class ParentedElement extends Element {

	private final ParentedElement parent;
	
	public ParentedElement(@Nullable ParentedElement parent, Element element) {
		super(element.getParser(), element.getSpec(), element.getEndTokenIndex(), element.getNode());
		
		this.parent = parent;
	}

	public ParentedElement getParent() {
		return parent;
	}

	@Nullable
	public ParentedElement findParentByLabel(String label) {
		ParentedElement current = parent;
		while (current != null) {
			if (label.equals(current.getLabel()))
				return current;
			current = current.parent;
		}
		return null;
	}

	@Nullable
	public ParentedElement findParentByRule(String ruleName) {
		ParentedElement current = parent;
		while (current != null) {
			if (ruleName.equals(current.getNode().getRuleSpec().getName()))
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
