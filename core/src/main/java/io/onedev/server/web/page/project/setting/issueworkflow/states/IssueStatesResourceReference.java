package io.onedev.server.web.page.project.setting.issueworkflow.states;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class IssueStatesResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public IssueStatesResourceReference() {
		super(IssueStatesResourceReference.class, "issue-states.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				IssueStatesResourceReference.class, "issue-states.css")));
		return dependencies;
	}

}
