package com.pmease.commons.wicket.behavior.dropdown;

import java.util.Arrays;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class DropdownResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public DropdownResourceReference() {
		super(DropdownBehavior.class, "dropdown.js");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Arrays.asList(
				JavaScriptHeaderItem.forReference(org.apache.wicket.bootstrap.Bootstrap.responsive()),
				JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(DropdownBehavior.class, "alignment.js")),
				CssHeaderItem.forReference(new CssResourceReference(DropdownBehavior.class, "dropdown.css")));
	}

}
