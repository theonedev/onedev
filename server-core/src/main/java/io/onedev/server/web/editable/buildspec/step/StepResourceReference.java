package io.onedev.server.web.editable.buildspec.step;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class StepResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public StepResourceReference() {
		super(StepResourceReference.class, "step.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				StepResourceReference.class, "step.css")));
		return dependencies;
	}

}
