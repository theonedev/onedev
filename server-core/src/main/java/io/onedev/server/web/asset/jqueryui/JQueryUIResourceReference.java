package io.onedev.server.web.asset.jqueryui;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class JQueryUIResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public JQueryUIResourceReference() {
		super(JQueryUIResourceReference.class, "jquery.ui.touch-punch.min.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(
				Application.get().getJavaScriptLibrarySettings().getJQueryReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				JQueryUIResourceReference.class, "jquery-ui.min.js")));
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(JQueryUIResourceReference.class, "jquery-ui.min.css")));
		return dependencies;
	}
	
}
