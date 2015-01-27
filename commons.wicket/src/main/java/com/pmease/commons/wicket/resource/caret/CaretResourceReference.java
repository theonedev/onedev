package com.pmease.commons.wicket.resource.caret;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class CaretResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final CaretResourceReference INSTANCE = new CaretResourceReference();
	
	private CaretResourceReference() {
		super(CaretResourceReference.class, "jquery.caret.js");
	}

}
