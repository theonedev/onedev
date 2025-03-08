package io.onedev.server.web.component.sideinfo;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class SideInfoResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public SideInfoResourceReference() {
		super(SideInfoResourceReference.class, "side-info.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
			SideInfoResourceReference.class, "side-info.css")));
		return dependencies;
	}
}
