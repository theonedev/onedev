package com.pmease.gitop.web.page.project.source.blob.renderer.syntax;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class GoogleCodePrettifyResourceReference extends JavaScriptResourceReference {
	private static final long serialVersionUID = 1L;

	private static final ResourceReference CSS = new CssResourceReference(
			GoogleCodePrettifyResourceReference.class, "res/prettify/prettify.css");
	
	public GoogleCodePrettifyResourceReference() {
		super(GoogleCodePrettifyResourceReference.class, "res/prettify/prettify.js");
	}


	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Iterables.concat(super.getDependencies(), 
				ImmutableList.of(CssHeaderItem.forReference(CSS)));
	}
}
