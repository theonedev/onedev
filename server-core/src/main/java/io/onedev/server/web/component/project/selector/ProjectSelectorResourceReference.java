package io.onedev.server.web.component.project.selector;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import io.onedev.server.web.asset.hotkeys.HotkeysResourceReference;
import io.onedev.server.web.asset.scrollintoview.ScrollIntoViewResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

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
