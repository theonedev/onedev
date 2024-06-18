package io.onedev.server.web.component.iteration.choice;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class IterationChoiceResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public IterationChoiceResourceReference() {
		super(IterationChoiceResourceReference.class, "iteration-choice.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(IterationChoiceResourceReference.class, "iteration-choice.css")));
		return dependencies;
	}

}
