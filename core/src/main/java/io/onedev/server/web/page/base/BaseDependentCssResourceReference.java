package io.onedev.server.web.page.base;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

public class BaseDependentCssResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public BaseDependentCssResourceReference(Class<?> scope, String name) {
		super(scope, name);
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseCssResourceReference()));
		return dependencies;
	}

}
