package com.gitplex.web.util.resourcebundle.testdata.b;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.gitplex.server.web.util.resourcebundle.ResourceBundle;

@ResourceBundle
public class Js1ResourceReferenceB extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public Js1ResourceReferenceB() {
		super(Js1ResourceReferenceB.class, "1.js");
	}
	
}
