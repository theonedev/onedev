package com.gitplex.server.web.behavior.infinitescroll;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.gitplex.server.web.assets.scrollintoview.ScrollIntoViewResourceReference;
import com.gitplex.server.web.page.CommonDependentResourceReference;

public class InfiniteScrollResourceReference extends CommonDependentResourceReference {

	private static final long serialVersionUID = 1L;
	
	public InfiniteScrollResourceReference() {
		super(InfiniteScrollResourceReference.class, "infinite-scroll.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> items = super.getDependencies();
		items.add(JavaScriptHeaderItem.forReference(new ScrollIntoViewResourceReference()));
		return items;
	}

}
