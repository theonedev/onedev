package io.onedev.server.web.page.test;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import java.util.List;

public class TestResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public TestResourceReference() {
		super(TestResourceReference.class, "test.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				TestResourceReference.class, "test.css")));
		return dependencies;
	}

}
