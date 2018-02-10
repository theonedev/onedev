package com.turbodev.server.web.page.project.pullrequest;

import org.apache.wicket.request.resource.CssResourceReference;

public class InvalidRequestResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public InvalidRequestResourceReference() {
		super(InvalidRequestResourceReference.class, "invalid-request.css");
	}

}
