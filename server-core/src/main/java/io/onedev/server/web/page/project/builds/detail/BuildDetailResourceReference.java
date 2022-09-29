package io.onedev.server.web.page.project.builds.detail;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class BuildDetailResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public BuildDetailResourceReference() {
		super(BuildDetailResourceReference.class, "build-detail.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				BuildDetailResourceReference.class, "build-detail.css")));
		return dependencies;
	}

}
