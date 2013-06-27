package com.pmease.commons.web.asset;

import java.util.Arrays;

import org.apache.wicket.bootstrap.Bootstrap;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class CommonResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public CommonResourceReference() {
		super(CommonResourceReference.class, "common.js");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Arrays.asList(
				JavaScriptHeaderItem.forReference(Bootstrap.responsive()),
				CssHeaderItem.forReference(new CssResourceReference(CommonResourceReference.class, "common.css")));
	}

}
