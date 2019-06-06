package io.onedev.server.web.asset.hover;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class HoverResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public HoverResourceReference() {
		super(HoverResourceReference.class, "jquery.hover.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings().getJQueryReference()));
		return dependencies;
	}
	
}
