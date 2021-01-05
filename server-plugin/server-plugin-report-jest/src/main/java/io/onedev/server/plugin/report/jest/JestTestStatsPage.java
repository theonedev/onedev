package io.onedev.server.plugin.report.jest;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.JestTestMetric;
import io.onedev.server.web.page.project.stats.buildmetric.BuildMetricStatsPage;

@SuppressWarnings("serial")
public class JestTestStatsPage extends BuildMetricStatsPage<JestTestMetric> {

	public JestTestStatsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Jest Test Statistics");
	}

}
