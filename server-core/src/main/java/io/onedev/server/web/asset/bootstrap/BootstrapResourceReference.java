package io.onedev.server.web.asset.bootstrap;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class BootstrapResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public BootstrapResourceReference() {
		super(BootstrapResourceReference.class, "js/bootstrap.bundle.min.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings().getJQueryReference()));
		dependencies.add(CssHeaderItem.forReference(new BootstrapCssResourceReference()));
		return dependencies;
	}

}
