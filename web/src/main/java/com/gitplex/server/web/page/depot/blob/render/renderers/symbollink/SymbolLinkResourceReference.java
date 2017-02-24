package com.gitplex.server.web.page.depot.blob.render.renderers.symbollink;

import org.apache.wicket.request.resource.CssResourceReference;

public class SymbolLinkResourceReference extends CssResourceReference {
	
	private static final long serialVersionUID = 1L;

	public SymbolLinkResourceReference() {
		super(SymbolLinkResourceReference.class, "symbol-link.css");
	}

}
