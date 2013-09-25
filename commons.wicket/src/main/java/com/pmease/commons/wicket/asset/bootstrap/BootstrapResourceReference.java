package com.pmease.commons.wicket.asset.bootstrap;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.asset.JQueryHeaderItem;

public class BootstrapResourceReference extends JavaScriptResourceReference {
	
	private static final long serialVersionUID = 1L;

	public static BootstrapResourceReference get() {
		return INSTANCE;
	}
	
	private static BootstrapResourceReference INSTANCE = new BootstrapResourceReference();
	
	private BootstrapResourceReference() {
		super(BootstrapResourceReference.class, "js/bootstrap.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		HeaderItem jquery = JQueryHeaderItem.get();
		HeaderItem stylesheet = CssHeaderItem.forReference(
				new CssResourceReference(BootstrapResourceReference.class, "css/bootstrap.css"));

		return Lists.newArrayList(jquery, stylesheet);
	}
}