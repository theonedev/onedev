package com.pmease.gitop.web.assets;

import org.apache.wicket.markup.head.JavaScriptHeaderItem;

public class PageHeaderItem {
	
	public static JavaScriptHeaderItem get() {
		return JavaScriptHeaderItem.forReference(PageResourceReference.get());
	}

}
