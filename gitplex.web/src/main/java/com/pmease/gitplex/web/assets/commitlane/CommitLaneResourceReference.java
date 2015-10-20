package com.pmease.gitplex.web.assets.commitlane;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class CommitLaneResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final CommitLaneResourceReference INSTANCE = new CommitLaneResourceReference();
	
	private CommitLaneResourceReference() {
		super(CommitLaneResourceReference.class, "commit-lane.js");
	}

}
