package com.pmease.commons.wicket.behavior.collapse;

import java.util.Arrays;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.asset.bootstrap.BootstrapHeaderItem;

public class CollapseResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static CollapseResourceReference get() {
		return INSTANCE;
	}
	
	private static CollapseResourceReference INSTANCE = new CollapseResourceReference();
	
	private CollapseResourceReference() {
		super(CollapseBehavior.class, "collapse.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Arrays.asList(BootstrapHeaderItem.get());
	}

}
