package io.onedev.server.web.asset.bootstrap;

import org.apache.wicket.request.resource.CssResourceReference;

public class BootstrapCssResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public BootstrapCssResourceReference() {
		super(BootstrapCssResourceReference.class, "css/bootstrap.min.css");
	}

}
