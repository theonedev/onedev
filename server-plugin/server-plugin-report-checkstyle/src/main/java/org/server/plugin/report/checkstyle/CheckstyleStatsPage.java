package org.server.plugin.report.checkstyle;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.CheckstyleMetric;
import io.onedev.server.web.page.project.stats.buildmetric.BuildMetricStatsPage;

@SuppressWarnings("serial")
public class CheckstyleStatsPage extends BuildMetricStatsPage<CheckstyleMetric> {

	public CheckstyleStatsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Checkstyle Statistics");
	}

}
