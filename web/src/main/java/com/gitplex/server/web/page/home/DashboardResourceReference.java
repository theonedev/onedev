package com.gitplex.server.web.page.home;

import org.apache.wicket.request.resource.CssResourceReference;

public class DashboardResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public DashboardResourceReference() {
		super(DashboardResourceReference.class, "dashboard.css");
	}

}
