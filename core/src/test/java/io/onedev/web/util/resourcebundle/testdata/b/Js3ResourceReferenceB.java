package io.onedev.web.util.resourcebundle.testdata.b;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import io.onedev.server.web.resourcebundle.ResourceBundle;

@ResourceBundle
public class Js3ResourceReferenceB extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public Js3ResourceReferenceB() {
		super(Js3ResourceReferenceB.class, "3.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(new Js2ResourceReferenceB()));
		return dependencies;
	}
	
}
