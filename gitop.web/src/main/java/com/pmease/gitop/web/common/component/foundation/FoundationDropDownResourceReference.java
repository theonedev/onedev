package com.pmease.gitop.web.common.component.foundation;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class FoundationDropDownResourceReference extends JavaScriptResourceReference {
	private static final long serialVersionUID = 1L;

	private static final ResourceReference CSS = new CssResourceReference(
			FoundationDropDownResourceReference.class, "res/css/dropdown.css");
	
	public FoundationDropDownResourceReference() {
		super(FoundationDropDownResourceReference.class, "res/js/foundation.dropdown.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Iterables.concat(super.getDependencies(),
				ImmutableList.of(
						JavaScriptHeaderItem.forReference(FoundationResourceReference.get()),
						CssHeaderItem.forReference(CSS),
						OnDomReadyHeaderItem.forScript("$(document).foundation()")));
	}
	
	private static FoundationDropDownResourceReference instance =
			new FoundationDropDownResourceReference();
	
	public static FoundationDropDownResourceReference get() {
		return instance;
	}
}
