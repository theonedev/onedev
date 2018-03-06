package io.onedev.server.web.behavior.infinitescroll;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.scrollintoview.ScrollIntoViewResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class InfiniteScrollResourceReference extends BaseDependentResourceReference {

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
