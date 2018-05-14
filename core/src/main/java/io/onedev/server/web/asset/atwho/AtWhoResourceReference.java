package io.onedev.server.web.asset.atwho;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class AtWhoResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public AtWhoResourceReference() {
		super(AtWhoResourceReference.class, "jquery.atwho.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings().getJQueryReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
				AtWhoResourceReference.class, "jquery.atwho-caret.js")));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				AtWhoResourceReference.class, "jquery.atwho.css")));
		return dependencies;
	}

}
