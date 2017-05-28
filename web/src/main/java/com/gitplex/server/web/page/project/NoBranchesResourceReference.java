package com.gitplex.server.web.page.project;

import org.apache.wicket.request.resource.CssResourceReference;

public class NoBranchesResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public NoBranchesResourceReference() {
		super(NoBranchesResourceReference.class, "no-branches.css");
	}

}
