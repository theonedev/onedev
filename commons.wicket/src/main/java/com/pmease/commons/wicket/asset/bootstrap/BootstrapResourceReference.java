package com.pmease.commons.wicket.asset.bootstrap;

import java.util.Arrays;

import org.apache.wicket.ajax.WicketEventJQueryResourceReference;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class BootstrapResourceReference extends JavaScriptResourceReference {
	
	private static final long serialVersionUID = 1L;

	public BootstrapResourceReference() {
		super(BootstrapResourceReference.class, "js/bootstrap.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		HeaderItem jquery = JavaScriptHeaderItem.forReference(WicketEventJQueryResourceReference.get());
		HeaderItem stylesheet = CssHeaderItem.forReference(
				new CssResourceReference(BootstrapResourceReference.class, "css/bootstrap.css"));
		HeaderItem themeStylesheet = CssHeaderItem.forReference(
				new CssResourceReference(BootstrapResourceReference.class, "css/bootstrap-theme.css"));

		return Arrays.asList(jquery, stylesheet, themeStylesheet);
	}
}