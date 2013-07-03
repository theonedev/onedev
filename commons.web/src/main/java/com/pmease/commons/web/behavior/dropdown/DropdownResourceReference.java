package com.pmease.commons.web.behavior.dropdown;

import java.util.Arrays;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.web.asset.bootstrap.BootstrapResourceReference;

public class DropdownResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public DropdownResourceReference() {
		super(DropdownResourceReference.class, "dropdown.js");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Arrays.asList(
				JavaScriptHeaderItem.forReference(new BootstrapResourceReference()),
				JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
						DropdownResourceReference.class, "alignment.js")),
				CssHeaderItem.forReference(new CssResourceReference(
						DropdownResourceReference.class, "dropdown.css")));
	}

}
