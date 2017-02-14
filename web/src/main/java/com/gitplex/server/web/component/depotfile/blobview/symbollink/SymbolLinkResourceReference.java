package com.gitplex.server.web.component.depotfile.blobview.symbollink;

import org.apache.wicket.request.resource.CssResourceReference;

public class SymbolLinkResourceReference extends CssResourceReference {
	
	private static final long serialVersionUID = 1L;

	public SymbolLinkResourceReference() {
		super(SymbolLinkResourceReference.class, "symbol-link.css");
	}

}
