package com.pmease.gitop.web.assets;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.pmease.commons.wicket.asset.CommonHeaderItem;

public class PageBaseResourceReference extends JavaScriptResourceReference {
	private static final long serialVersionUID = 1L;

	public PageBaseResourceReference() {
		super(PageBaseResourceReference.class, "js/page.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Iterables.concat(super.getDependencies(),
				ImmutableList.of(
						new PriorityHeaderItem(JavaScriptHeaderItem.forReference(AssetLocator.MODERNIZR_JS)),
						new PriorityHeaderItem(CommonHeaderItem.get()),
						new PriorityHeaderItem(CssHeaderItem.forReference(AssetLocator.ICONS_CSS)),
						new PriorityHeaderItem(CssHeaderItem.forReference(AssetLocator.BASE_CSS)),
						
						CssHeaderItem.forReference(AssetLocator.PAGE_CSS)));
	}
	
	public static final PageBaseResourceReference instance =
			new PageBaseResourceReference();
	
	public static PageBaseResourceReference getInstance() {
		return instance;
	}
}
