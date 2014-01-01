package com.pmease.gitop.web.common.wicket.bootstrap.jasny;

import org.apache.wicket.request.resource.CssResourceReference;

/**
 * A CSS reference that loads the CSS resources needed by Jasny Twitter
 * Bootstrap components.
 */
@SuppressWarnings("serial")
public class JasnyCssReference extends CssResourceReference {
	public static final JasnyCssReference INSTANCE = new JasnyCssReference();

	private JasnyCssReference() {
		super(JasnyCssReference.class, "res/jasny.css");
	}
}