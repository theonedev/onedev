package io.onedev.server.web.component.milestone.choice;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class MilestoneChoiceResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public MilestoneChoiceResourceReference() {
		super(MilestoneChoiceResourceReference.class, "milestone-choice.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(MilestoneChoiceResourceReference.class, "milestone-choice.css")));
		return dependencies;
	}

}
