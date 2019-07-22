package io.onedev.web.util.resourcebundle.testdata.b;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class Js2ResourceReferenceB extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public Js2ResourceReferenceB() {
		super(Js2ResourceReferenceB.class, "2.js");
	}
	
	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(new Js1ResourceReferenceB()));
		return dependencies;
	}
	
}
