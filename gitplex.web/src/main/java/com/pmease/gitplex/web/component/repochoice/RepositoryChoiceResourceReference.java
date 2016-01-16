package com.pmease.gitplex.web.component.repochoice;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class RepositoryChoiceResourceReference extends JavaScriptResourceReference {
	
	private static final long serialVersionUID = 1L;

	public static final RepositoryChoiceResourceReference INSTANCE = new RepositoryChoiceResourceReference();
	
	private RepositoryChoiceResourceReference() {
		super(RepositoryChoiceResourceReference.class, "repo-choice.js");
	}

}
