package io.onedev.server.web.page.project.builds.detail.report;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Project;
import io.onedev.server.model.support.BuildMetric;
import io.onedev.server.search.buildmetric.BuildMetricQuery;
import io.onedev.server.search.buildmetric.BuildMetricQueryParser;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.stats.buildmetric.BuildMetricStatsPage;

@SuppressWarnings("serial")
class BuildReportTabOptionPanel extends Panel {

	private final Class<? extends BuildMetricStatsPage<?>> statsPageClass;
	
	private final String reportName;
	
	public BuildReportTabOptionPanel(String id, Class<? extends BuildMetricStatsPage<?>> statsPageClass, String reportName) {
		super(id);
		this.statsPageClass = statsPageClass;
		this.reportName = reportName;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		String query = String.format("%s \"last month\" and \"%s\" %s \"%s\"", 
				BuildMetricQuery.getRuleName(BuildMetricQueryParser.Since), 
				BuildMetric.NAME_REPORT, 
				BuildMetricQuery.getRuleName(BuildMetricQueryParser.Is), 
				reportName);
		PageParameters params = BuildMetricStatsPage.paramsOf(Project.get(), query);
		add(new ViewStateAwarePageLink<>("link", statsPageClass, params));
	}

}
