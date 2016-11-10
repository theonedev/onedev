package com.gitplex.commons.wicket.assets.areyousure;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class AreYouSureResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public AreYouSureResourceReference() {
		super(AreYouSureResourceReference.class, "jquery.are-you-sure.patched.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings().getJQueryReference()));
		return dependencies;
	}
	
}
