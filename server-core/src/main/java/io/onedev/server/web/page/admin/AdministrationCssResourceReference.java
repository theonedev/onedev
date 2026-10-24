package io.onedev.server.web.page.admin;

import java.io.Serial;

import org.apache.wicket.request.resource.CssResourceReference;

public class AdministrationCssResourceReference extends CssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public AdministrationCssResourceReference() {
		super(AdministrationCssResourceReference.class, "administration.css");
	}

}
