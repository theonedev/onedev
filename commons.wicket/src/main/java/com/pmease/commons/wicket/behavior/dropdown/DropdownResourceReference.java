package com.pmease.commons.wicket.behavior.dropdown;

import java.util.Arrays;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.asset.bootstrap.BootstrapHeaderItem;

public class DropdownResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static DropdownResourceReference get() {
		return INSTANCE;
	}
	
	private static DropdownResourceReference INSTANCE = new DropdownResourceReference();
	
	private DropdownResourceReference() {
		super(DropdownResourceReference.class, "dropdown.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Arrays.asList(
				BootstrapHeaderItem.get(),
				JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
						DropdownResourceReference.class, "alignment.js")),
				CssHeaderItem.forReference(new CssResourceReference(
						DropdownResourceReference.class, "dropdown.css")));
	}

}
