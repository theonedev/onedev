package com.gitplex.commons.wicket.util.testdata.b;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.gitplex.commons.wicket.resourcebundle.ResourceBundle;

@ResourceBundle
public class Js1ResourceReferenceB extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public Js1ResourceReferenceB() {
		super(Js1ResourceReferenceB.class, "1.js");
	}
	
}
