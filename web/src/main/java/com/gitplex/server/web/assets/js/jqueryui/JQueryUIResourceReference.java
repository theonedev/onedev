package com.gitplex.server.web.assets.js.jqueryui;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

/*
 * Do not include jquery ui css here as otherwise the search result resizing does not work
 */
public class JQueryUIResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public JQueryUIResourceReference() {
		super(JQueryUIResourceReference.class, "jquery-ui.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(
				Application.get().getJavaScriptLibrarySettings().getJQueryReference()));
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(JQueryUIResourceReference.class, "jquery-ui.css")));
		return dependencies;
	}
	
}
