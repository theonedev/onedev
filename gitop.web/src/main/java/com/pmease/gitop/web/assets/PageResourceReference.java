package com.pmease.gitop.web.assets;

import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.asset.CommonHeaderItem;

public class PageResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static PageResourceReference get() {
		return INSTANCE;
	}
	
	private static PageResourceReference INSTANCE = new PageResourceReference();
	
	private PageResourceReference() {
		super(PageResourceReference.class, "page.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Lists.newArrayList(
				JavaScriptReferenceHeaderItem.forReference(AssetLocator.MODERNIZR_JS),
				CommonHeaderItem.get(),
				CssReferenceHeaderItem.forReference(AssetLocator.ICONS_CSS),
				CssReferenceHeaderItem.forReference(AssetLocator.BASE_CSS),
				CssReferenceHeaderItem.forReference(AssetLocator.PAGE_CSS));

	}

}
