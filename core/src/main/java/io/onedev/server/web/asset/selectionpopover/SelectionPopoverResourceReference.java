package io.onedev.server.web.asset.selectionpopover;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class SelectionPopoverResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public SelectionPopoverResourceReference() {
		super(SelectionPopoverResourceReference.class, "jquery.selectionpopover.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings().getJQueryReference()));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				SelectionPopoverResourceReference.class, "jquery.selectionpopover.css")));
		return dependencies;
	}
	
}
