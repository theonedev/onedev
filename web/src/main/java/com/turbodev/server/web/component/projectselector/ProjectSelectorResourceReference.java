package com.turbodev.server.web.component.projectselector;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.turbodev.server.web.asset.hotkeys.HotkeysResourceReference;
import com.turbodev.server.web.asset.scrollintoview.ScrollIntoViewResourceReference;
import com.turbodev.server.web.page.base.BaseDependentResourceReference;

public class ProjectSelectorResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public ProjectSelectorResourceReference() {
		super(ProjectSelectorResourceReference.class, "project-selector.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new HotkeysResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new ScrollIntoViewResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(ProjectSelectorResourceReference.class, "project-selector.css")));
		return dependencies;
	}

}
