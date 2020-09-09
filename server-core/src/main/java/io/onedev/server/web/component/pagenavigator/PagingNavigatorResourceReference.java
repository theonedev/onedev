package io.onedev.server.web.component.pagenavigator;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class PagingNavigatorResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public PagingNavigatorResourceReference() {
		super(PagingNavigatorResourceReference.class, "paging-navigator.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				PagingNavigatorResourceReference.class, "paging-navigator.css")));
		return dependencies;
	}

}
