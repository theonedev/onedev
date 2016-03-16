package com.pmease.gitplex.web.page.organization;

import org.apache.wicket.request.resource.CssResourceReference;

public class OrganizationResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public static final OrganizationResourceReference INSTANCE = new OrganizationResourceReference();
	
	private OrganizationResourceReference() {
		super(OrganizationResourceReference.class, "organization.css");
	}

}
