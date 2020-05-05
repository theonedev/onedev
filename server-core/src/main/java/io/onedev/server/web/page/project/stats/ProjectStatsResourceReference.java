package io.onedev.server.web.page.project.stats;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import io.onedev.server.web.asset.day.DayResourceReference;
import io.onedev.server.web.asset.doneevents.DoneEventsResourceReference;
import io.onedev.server.web.asset.echarts.EChartsResourceReference;
import io.onedev.server.web.asset.hover.HoverResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class ProjectStatsResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public ProjectStatsResourceReference() {
		super(ProjectStatsResourceReference.class, "project-stats.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new EChartsResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new DoneEventsResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new HoverResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new DayResourceReference()));
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(ProjectStatsResourceReference.class, "project-stats.css")));
		return dependencies;
	}
	
}
