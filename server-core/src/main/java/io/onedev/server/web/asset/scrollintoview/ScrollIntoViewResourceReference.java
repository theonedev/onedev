package io.onedev.server.web.asset.scrollintoview;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import io.onedev.server.web.asset.jqueryui.JQueryUIResourceReference;

public class ScrollIntoViewResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public ScrollIntoViewResourceReference() {
		super(ScrollIntoViewResourceReference.class, "jquery.scrollintoview.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(new JQueryUIResourceReference()));
		return dependencies;
	}
	
}
