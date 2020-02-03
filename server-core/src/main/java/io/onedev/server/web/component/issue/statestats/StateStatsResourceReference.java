package io.onedev.server.web.component.issue.statestats;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class StateStatsResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public StateStatsResourceReference() {
		super(StateStatsResourceReference.class, "state-stats.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				StateStatsResourceReference.class, "state-stats.css")));
		return dependencies;
	}

}
