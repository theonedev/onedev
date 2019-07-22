package io.onedev.web.util.resourcebundle.testdata.c;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class Js1ResourceReferenceC extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public Js1ResourceReferenceC() {
		super(Js1ResourceReferenceC.class, "1.js");
	}
	
	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(Js1ResourceReferenceC.class, "1.css")));
		return dependencies;
	}
	
}
