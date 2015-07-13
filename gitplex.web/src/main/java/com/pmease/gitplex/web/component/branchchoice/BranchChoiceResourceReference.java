package com.pmease.gitplex.web.component.branchchoice;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class BranchChoiceResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final BranchChoiceResourceReference INSTANCE = new BranchChoiceResourceReference();
	
	private BranchChoiceResourceReference() {
		super(BranchChoiceResourceReference.class, "branch-choice.js");
	}

}
