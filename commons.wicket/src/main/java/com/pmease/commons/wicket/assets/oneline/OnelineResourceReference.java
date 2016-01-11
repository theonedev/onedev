package com.pmease.commons.wicket.assets.oneline;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class OnelineResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final OnelineResourceReference INSTANCE = new OnelineResourceReference();
	
	private OnelineResourceReference() {
		super(OnelineResourceReference.class, "jquery.oneline.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				OnelineResourceReference.class, "jquery.oneline.css")));
		return dependencies;
	}

}
