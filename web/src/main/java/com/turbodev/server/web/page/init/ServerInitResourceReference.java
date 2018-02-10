package com.turbodev.server.web.page.init;

import org.apache.wicket.request.resource.CssResourceReference;

public class ServerInitResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public ServerInitResourceReference() {
		super(ServerInitResourceReference.class, "server-init.css");
	}

}
