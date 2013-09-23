package com.pmease.commons.wicket.asset.bootstrap;

import org.apache.wicket.markup.head.JavaScriptHeaderItem;

public class BootstrapHeaderItem {
	
	public static JavaScriptHeaderItem get() {
		return JavaScriptHeaderItem.forReference(BootstrapResourceReference.get());
	}

}
