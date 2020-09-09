package io.onedev.server.web.page.layout;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class LayoutResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public LayoutResourceReference() {
		super(LayoutResourceReference.class, "layout.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				LayoutResourceReference.class, "layout.css")));
		return dependencies;
	}

}
