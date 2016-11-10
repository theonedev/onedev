package com.gitplex.commons.wicket.behavior.infinitescroll;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.gitplex.commons.wicket.assets.scrollintoview.ScrollIntoViewResourceReference;
import com.gitplex.commons.wicket.page.CommonDependentResourceReference;

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
