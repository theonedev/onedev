package com.pmease.gitplex.web.editable.directory;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class DirectoryChoiceResourceReference extends JavaScriptResourceReference {
	
	private static final long serialVersionUID = 1L;

	public static final DirectoryChoiceResourceReference INSTANCE = new DirectoryChoiceResourceReference();
	
	private DirectoryChoiceResourceReference() {
		super(DirectoryChoiceResourceReference.class, "directory-choice.js");
	}

}
