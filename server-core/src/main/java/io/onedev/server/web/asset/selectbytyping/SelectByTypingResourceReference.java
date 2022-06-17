package io.onedev.server.web.asset.selectbytyping;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.doneevents.DoneEventsResourceReference;
import io.onedev.server.web.asset.hotkeys.HotkeysResourceReference;
import io.onedev.server.web.page.base.BaseCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class SelectByTypingResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public SelectByTypingResourceReference() {
		super(SelectByTypingResourceReference.class, "jquery.selectbytyping.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(new DoneEventsResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new HotkeysResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseCssResourceReference()));
		return dependencies;
	}
	
}
