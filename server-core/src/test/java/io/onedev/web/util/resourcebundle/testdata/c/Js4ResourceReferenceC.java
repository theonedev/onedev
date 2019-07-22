package io.onedev.web.util.resourcebundle.testdata.c;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import io.onedev.server.web.resourcebundle.ResourceBundle;

@ResourceBundle
public class Js4ResourceReferenceC extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public Js4ResourceReferenceC() {
		super(Js4ResourceReferenceC.class, "4.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(new Js3ResourceReferenceC()));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(Js4ResourceReferenceC.class, "4.css")));
		return dependencies;
	}
	
}
