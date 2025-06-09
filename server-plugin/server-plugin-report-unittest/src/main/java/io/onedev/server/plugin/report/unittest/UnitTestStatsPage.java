package io.onedev.server.plugin.report.unittest;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.UnitTestMetric;
import io.onedev.server.web.page.project.stats.buildmetric.BuildMetricStatsPage;

public class UnitTestStatsPage extends BuildMetricStatsPage<UnitTestMetric> {

	public UnitTestStatsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Unit Test Statistics"));
	}

}
