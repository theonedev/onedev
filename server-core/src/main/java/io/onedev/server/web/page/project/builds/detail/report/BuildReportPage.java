package io.onedev.server.web.page.project.builds.detail.report;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Build;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;

@SuppressWarnings("serial")
public abstract class BuildReportPage extends BuildDetailPage {

	private static final String PARAM_REPORT = "report";

	private final String reportName;
	
	public BuildReportPage(PageParameters params) {
		super(params);
		
		reportName = params.get(PARAM_REPORT).toString();
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canAccessReport(getBuild(), reportName);
	}
	
	public String getReportName() {
		return reportName;
	}

	public static PageParameters paramsOf(Build build, String reportName) {
		PageParameters params = paramsOf(build);
		params.add(PARAM_REPORT, reportName);
		return params;
	}
	
}
