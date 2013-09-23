package com.pmease.commons.wicket.asset;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.JQueryResourceReference;

public class JQueryHeaderItem {
	
	public static JavaScriptHeaderItem get() {
		ResourceReference backingLibraryReference;
		if (Application.exists()) {
			backingLibraryReference = Application.get()
				.getJavaScriptLibrarySettings()
				.getJQueryReference();
		} else {
			backingLibraryReference = JQueryResourceReference.get();
		}
		return JavaScriptHeaderItem.forReference(backingLibraryReference);
	}
	
}
