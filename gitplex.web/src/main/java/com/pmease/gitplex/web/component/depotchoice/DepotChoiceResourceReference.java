package com.pmease.gitplex.web.component.depotchoice;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class DepotChoiceResourceReference extends JavaScriptResourceReference {
	
	private static final long serialVersionUID = 1L;

	public static final DepotChoiceResourceReference INSTANCE = new DepotChoiceResourceReference();
	
	private DepotChoiceResourceReference() {
		super(DepotChoiceResourceReference.class, "depot-choice.js");
	}

}
