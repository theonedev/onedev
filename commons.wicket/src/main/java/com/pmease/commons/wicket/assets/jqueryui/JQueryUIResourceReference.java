package com.pmease.commons.wicket.assets.jqueryui;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class JQueryUIResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final JQueryUIResourceReference INSTANCE = new JQueryUIResourceReference();
	
	private JQueryUIResourceReference() {
		super(JQueryUIResourceReference.class, "jquery-ui.min.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				JQueryUIResourceReference.class, "jquery-ui.min.css")));
		return dependencies;
	}
	
}
