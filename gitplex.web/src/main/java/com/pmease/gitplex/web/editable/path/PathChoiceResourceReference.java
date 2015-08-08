package com.pmease.gitplex.web.editable.path;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class PathChoiceResourceReference extends JavaScriptResourceReference {
	
	private static final long serialVersionUID = 1L;

	public static final PathChoiceResourceReference INSTANCE = new PathChoiceResourceReference();
	
	private PathChoiceResourceReference() {
		super(PathChoiceResourceReference.class, "path-choice.js");
	}

}
