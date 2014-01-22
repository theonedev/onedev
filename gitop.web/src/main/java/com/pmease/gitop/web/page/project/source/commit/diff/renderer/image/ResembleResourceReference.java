package com.pmease.gitop.web.page.project.source.commit.diff.renderer.image;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

@SuppressWarnings("serial")
public class ResembleResourceReference extends JavaScriptResourceReference {

	public ResembleResourceReference() {
		super(ResembleResourceReference.class, "res/resemble.js");
	}

	private static ResembleResourceReference instance =
			new ResembleResourceReference();
	
	public static ResembleResourceReference getInstance() {
		return instance;
	}
}
