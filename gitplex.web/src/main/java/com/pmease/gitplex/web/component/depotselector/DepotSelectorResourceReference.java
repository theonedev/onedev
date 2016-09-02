package com.pmease.gitplex.web.component.depotselector;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.assets.scrollintoview.ScrollIntoViewResourceReference;
import com.pmease.gitplex.web.page.base.BaseDependentResourceReference;

public class DepotSelectorResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public DepotSelectorResourceReference() {
		super(DepotSelectorResourceReference.class, "depot-selector.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new HotkeysResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new ScrollIntoViewResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(DepotSelectorResourceReference.class, "depot-selector.css")));
		return dependencies;
	}

}
