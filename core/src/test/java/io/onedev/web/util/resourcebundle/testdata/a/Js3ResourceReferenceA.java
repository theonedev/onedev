package io.onedev.web.util.resourcebundle.testdata.a;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class Js3ResourceReferenceA extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public Js3ResourceReferenceA() {
		super(Js3ResourceReferenceA.class, "3.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(new Js2ResourceReferenceA()));
		return dependencies;
	}
	
}
