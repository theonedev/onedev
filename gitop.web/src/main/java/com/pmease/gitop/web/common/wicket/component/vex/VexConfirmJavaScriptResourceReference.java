package com.pmease.gitop.web.common.wicket.component.vex;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

@SuppressWarnings("serial")
public class VexConfirmJavaScriptResourceReference extends JavaScriptResourceReference {

	public VexConfirmJavaScriptResourceReference() {
		super(VexConfirmJavaScriptResourceReference.class, "js/vex.confirm.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Iterables.concat(super.getDependencies(),
				ImmutableList.of(JavaScriptHeaderItem.forReference(VexJavaScriptResourceReference.get())));
	}
	
	private static final VexConfirmJavaScriptResourceReference instance =
			new VexConfirmJavaScriptResourceReference();
	
	public static VexConfirmJavaScriptResourceReference get() {
		return instance;
	}
}
