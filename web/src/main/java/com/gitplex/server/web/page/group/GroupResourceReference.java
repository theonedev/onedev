package com.gitplex.server.web.page.group;

import org.apache.wicket.request.resource.CssResourceReference;

public class GroupResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public GroupResourceReference() {
		super(GroupResourceReference.class, "group.css");
	}

}
