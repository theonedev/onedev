package io.onedev.server.web.util.resourcebundle.testdata.a;

import java.io.Serial;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class Js1ResourceReferenceA extends JavaScriptResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public Js1ResourceReferenceA() {
		super(Js1ResourceReferenceA.class, "1.js");
	}
	
}
