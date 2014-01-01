package com.pmease.gitop.web.common.wicket.bootstrap.jasny;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.resource.JQueryPluginResourceReference;

/**
 * A JavaScript reference that loads the JavaScript resources needed by Jasny
 * Twitter Bootstrap components.
 */
@SuppressWarnings("serial")
public class JasnyJsReference extends JQueryPluginResourceReference {
	public static final JasnyJsReference INSTANCE = new JasnyJsReference();

	private JasnyJsReference() {
		super(JasnyJsReference.class, "res/jasny.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		List<HeaderItem> deps = new ArrayList<HeaderItem>();
		for (HeaderItem dep : super.getDependencies()) {
			deps.add(dep);
		}
		
		deps.add(CssHeaderItem.forReference(JasnyCssReference.INSTANCE));
		return deps;
	}
}