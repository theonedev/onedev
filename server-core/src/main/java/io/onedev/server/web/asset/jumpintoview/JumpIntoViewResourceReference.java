package io.onedev.server.web.asset.jumpintoview;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class JumpIntoViewResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public JumpIntoViewResourceReference() {
		super(JumpIntoViewResourceReference.class, "jquery.jumpintoview.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings().getJQueryReference()));
		return dependencies;
	}
	
}
