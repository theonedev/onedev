package com.pmease.gitplex.web.component.repochoice;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class RepoChoiceResourceReference extends JavaScriptResourceReference {
	
	private static final long serialVersionUID = 1L;

	public static final RepoChoiceResourceReference INSTANCE = new RepoChoiceResourceReference();
	
	private RepoChoiceResourceReference() {
		super(RepoChoiceResourceReference.class, "repo-choice.js");
	}

}
