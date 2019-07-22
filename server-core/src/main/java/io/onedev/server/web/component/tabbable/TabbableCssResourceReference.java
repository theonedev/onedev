package io.onedev.server.web.component.tabbable;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.component.menu.MenuCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class TabbableCssResourceReference extends BaseDependentCssResourceReference {

	private static final long serialVersionUID = 1L;

	public TabbableCssResourceReference() {
		super(TabbableCssResourceReference.class, "tabbable.css");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new MenuCssResourceReference()));
		return dependencies;
	}

}
