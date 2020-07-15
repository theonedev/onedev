package io.onedev.server.web.asset.fontawesome;

import org.apache.wicket.request.resource.CssResourceReference;

public class FontAwesomeCssResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public FontAwesomeCssResourceReference() {
		super(FontAwesomeCssResourceReference.class, "css/font-awesome.min.css");
	}

}
