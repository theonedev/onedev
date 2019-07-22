package io.onedev.web.util.resourcebundle.testdata.a;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class Js2ResourceReferenceA extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public Js2ResourceReferenceA() {
		super(Js2ResourceReferenceA.class, "2.js");
	}
	
	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(new Js1ResourceReferenceA()));
		return dependencies;
	}
	
}
