package io.onedev.server.web.page.test;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.selectbykey.SelectByKeyResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class TestResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public TestResourceReference() {
		super(TestResourceReference.class, "test.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new SelectByKeyResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				TestResourceReference.class, "test.css")));
		return dependencies;
	}

}
