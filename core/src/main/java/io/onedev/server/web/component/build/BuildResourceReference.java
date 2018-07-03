package io.onedev.server.web.component.build;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class BuildResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public BuildResourceReference() {
		super(BuildResourceReference.class, "build.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				BuildResourceReference.class, "build.css")));
		return dependencies;
	}

}
