package com.gitplex.server.web.assets.js.bootstrapmarkdown;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class BootstrapMarkdownResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public BootstrapMarkdownResourceReference() {
		super(BootstrapMarkdownResourceReference.class, "bootstrap-markdown.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(
				Application.get().getJavaScriptLibrarySettings().getJQueryReference()));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				BootstrapMarkdownResourceReference.class, "bootstrap-markdown.min.css")));
		return dependencies;
	}

}
