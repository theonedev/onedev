package com.pmease.commons.wicket.assets.clearable;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class ClearableResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final ClearableResourceReference INSTANCE = new ClearableResourceReference();
	
	private ClearableResourceReference() {
		super(ClearableResourceReference.class, "jquery.clearable.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				ClearableResourceReference.class, "jquery.clearable.css")));
		return dependencies;
	}
	
}
