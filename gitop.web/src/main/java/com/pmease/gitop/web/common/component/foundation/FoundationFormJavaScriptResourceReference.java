package com.pmease.gitop.web.common.component.foundation;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class FoundationFormJavaScriptResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public FoundationFormJavaScriptResourceReference() {
		super(FoundationFormJavaScriptResourceReference.class, "res/js/foundation.forms.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Iterables.concat(
				super.getDependencies(),
				Lists.newArrayList(JavaScriptHeaderItem.forReference(
						FoundationJavaScriptResourceReference.get())));
	}
	
	private static final FoundationFormJavaScriptResourceReference instance =
			new FoundationFormJavaScriptResourceReference();
	
	public static FoundationFormJavaScriptResourceReference get() {
		return instance;
	}
}
