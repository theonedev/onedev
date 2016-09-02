package com.pmease.gitplex.web.component.depotfile.blobsearch.instant;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.pmease.commons.wicket.assets.doneevents.DoneEventsResourceReference;
import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.assets.scrollintoview.ScrollIntoViewResourceReference;
import com.pmease.gitplex.web.page.base.BaseDependentCssResourceReference;
import com.pmease.gitplex.web.page.base.BaseDependentResourceReference;

public class InstantSearchResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public InstantSearchResourceReference() {
		super(InstantSearchResourceReference.class, "instant-search.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new ScrollIntoViewResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new DoneEventsResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new HotkeysResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(InstantSearchResourceReference.class, "instant-search.css")));
		return dependencies;
	}

}
