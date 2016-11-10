package com.gitplex.commons.antlr.codeassist;

import javax.annotation.Nullable;

import com.gitplex.commons.antlr.parser.Element;

public class ParentedElement extends Element {

	private final ParentedElement parent;
	
	public ParentedElement(@Nullable ParentedElement parent, Element element) {
		super(element.getParser(), element.getSpec(), element.getPosition(), element.getState());
		
		this.parent = parent;
	}

	/**
	 * Get parent element
	 * 
	 * @return
	 * 			parent element, or <tt>null</tt> if current element is root
	 */
	@Nullable
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
			if (ruleName.equals(current.getState().getRuleSpec().getName()))
				return current;
			current = current.parent;
		}
		return null;
	}
	
	/**
	 * Get root element
	 * 
	 * @return
	 * 			root element
	 */
	public ParentedElement getRoot() {
		if (parent == null)
			return this;
		else
			return parent.getRoot();
	}

}
