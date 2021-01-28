package org.server.plugin.report.clover;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.CloverMetric;
import io.onedev.server.web.page.project.stats.buildmetric.BuildMetricStatsPage;

@SuppressWarnings("serial")
public class CloverStatsPage extends BuildMetricStatsPage<CloverMetric> {

	public CloverStatsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Clover Coverage Statistics");
	}

}
