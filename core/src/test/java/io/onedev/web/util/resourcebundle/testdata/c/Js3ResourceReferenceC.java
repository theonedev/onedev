package io.onedev.web.util.resourcebundle.testdata.c;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class Js3ResourceReferenceC extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public Js3ResourceReferenceC() {
		super(Js3ResourceReferenceC.class, "3.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(new Js1ResourceReferenceC()));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(Js3ResourceReferenceC.class, "3.css")));
		return dependencies;
	}
	
}
