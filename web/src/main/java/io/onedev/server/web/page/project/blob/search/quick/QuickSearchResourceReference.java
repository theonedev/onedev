package io.onedev.server.web.page.project.blob.search.quick;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.doneevents.DoneEventsResourceReference;
import io.onedev.server.web.asset.hotkeys.HotkeysResourceReference;
import io.onedev.server.web.asset.scrollintoview.ScrollIntoViewResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class QuickSearchResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public QuickSearchResourceReference() {
		super(QuickSearchResourceReference.class, "quick-search.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new ScrollIntoViewResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new DoneEventsResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new HotkeysResourceReference()));
		dependencies.add(CssHeaderItem.forReference(
				new BaseDependentCssResourceReference(QuickSearchResourceReference.class, "quick-search.css")));
		return dependencies;
	}

}
