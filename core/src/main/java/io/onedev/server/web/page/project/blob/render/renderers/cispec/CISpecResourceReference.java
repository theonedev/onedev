package io.onedev.server.web.page.project.blob.render.renderers.cispec;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class CISpecResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public CISpecResourceReference() {
		super(CISpecResourceReference.class, "ci-spec.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				CISpecResourceReference.class, "ci-spec.css")));
		return dependencies;
	}

}
