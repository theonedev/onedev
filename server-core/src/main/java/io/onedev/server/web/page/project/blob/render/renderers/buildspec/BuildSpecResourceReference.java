package io.onedev.server.web.page.project.blob.render.renderers.buildspec;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.doneevents.DoneEventsResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class BuildSpecResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public BuildSpecResourceReference() {
		super(BuildSpecResourceReference.class, "build-spec.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new DoneEventsResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				BuildSpecResourceReference.class, "build-spec.css")));
		return dependencies;
	}

}
