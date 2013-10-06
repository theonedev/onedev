package com.pmease.gitop.web.common.component.vex;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

@SuppressWarnings("serial")
public class VexJavaScriptResourceReference extends JavaScriptResourceReference {

	private static ResourceReference VEX_CSS = new CssResourceReference(VexJavaScriptResourceReference.class, "css/vex.css");
	private static ResourceReference VEX_THEME_CSS = new CssResourceReference(VexJavaScriptResourceReference.class, "css/vex-theme-wireframe.css");
	
	public VexJavaScriptResourceReference() {
		super(VexJavaScriptResourceReference.class, "js/vex.combined.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Iterables.concat(super.getDependencies(),
				ImmutableList.of(
						CssHeaderItem.forReference(VEX_CSS),
						CssHeaderItem.forReference(VEX_THEME_CSS)));
	}
	
	private static final VexJavaScriptResourceReference instance =
			new VexJavaScriptResourceReference();
	
	public static VexJavaScriptResourceReference get() {
		return instance;
	}
}
