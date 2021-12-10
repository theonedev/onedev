package io.onedev.server.web.asset.pickr;

import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class PickrResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public PickrResourceReference() {
		super(PickrResourceReference.class, "pickr.min.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings().getJQueryReference()));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(PickrResourceReference.class, "classic.min.css")));
		return dependencies;
	}

}
