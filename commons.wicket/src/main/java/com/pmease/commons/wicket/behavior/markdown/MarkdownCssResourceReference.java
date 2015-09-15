package com.pmease.commons.wicket.behavior.markdown;

import org.apache.wicket.request.resource.CssResourceReference;

@SuppressWarnings("serial")
public class MarkdownCssResourceReference extends CssResourceReference {

	public static final MarkdownCssResourceReference INSTANCE = new MarkdownCssResourceReference();
	
	private MarkdownCssResourceReference() {
		super(MarkdownCssResourceReference.class, "markdown.css");
	}

}
