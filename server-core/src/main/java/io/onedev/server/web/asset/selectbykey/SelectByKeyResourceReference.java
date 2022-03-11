package io.onedev.server.web.asset.selectbykey;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.doneevents.DoneEventsResourceReference;
import io.onedev.server.web.asset.hotkeys.HotkeysResourceReference;
import io.onedev.server.web.asset.scrollintoview.ScrollIntoViewResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class SelectByKeyResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public SelectByKeyResourceReference() {
		super(SelectByKeyResourceReference.class, "jquery.selectbykey.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(new DoneEventsResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new ScrollIntoViewResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new HotkeysResourceReference()));
		return dependencies;
	}
	
}
