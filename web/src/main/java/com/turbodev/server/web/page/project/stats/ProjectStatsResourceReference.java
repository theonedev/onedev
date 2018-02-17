package com.turbodev.server.web.page.project.stats;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.turbodev.server.web.asset.chart.ChartResourceReference;
import com.turbodev.server.web.page.base.BaseDependentResourceReference;

public class ProjectStatsResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public ProjectStatsResourceReference() {
		super(ProjectStatsResourceReference.class, "project-stats.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new ChartResourceReference()));
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(ProjectStatsResourceReference.class, "project-stats.css")));
		return dependencies;
	}
	
}
