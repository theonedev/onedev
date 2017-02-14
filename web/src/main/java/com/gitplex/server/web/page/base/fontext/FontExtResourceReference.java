package com.gitplex.server.web.page.base.fontext;

import org.apache.wicket.request.resource.CssResourceReference;

public class FontExtResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public FontExtResourceReference() {
		super(FontExtResourceReference.class, "fontext.css");
	}
	
}
