package com.pmease.commons.wicket.asset;

import org.apache.wicket.markup.head.JavaScriptHeaderItem;

public class CommonHeaderItem {
	
	public static JavaScriptHeaderItem get() {
		return JavaScriptHeaderItem.forReference(CommonResourceReference.get());
	}

}
