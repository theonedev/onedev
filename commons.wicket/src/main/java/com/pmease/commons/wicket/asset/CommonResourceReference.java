package com.pmease.commons.wicket.asset;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.asset.bootstrap.BootstrapHeaderItem;

/**
 * Common resource is intended to provide a common look&feel for all products using 
 * commons.wicket 
 * 
 * @author robin
 *
 */
public class CommonResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static CommonResourceReference get() {
		return INSTANCE;
	}
	
	private static CommonResourceReference INSTANCE = new CommonResourceReference();
	
	private CommonResourceReference() {
		super(CommonResourceReference.class, "common.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Lists.newArrayList(
				BootstrapHeaderItem.get(),
				CssHeaderItem.forReference(new CssResourceReference(CommonResourceReference.class, "common.css")));
	}

}
