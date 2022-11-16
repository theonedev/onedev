package io.onedev.server.web.page.project.pullrequests.detail;

import java.io.Serializable;

import org.apache.wicket.Component;

public abstract class PullRequestSummaryPart implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String reportName;
	
	public PullRequestSummaryPart(String reportName) {
		this.reportName = reportName;
	}
	
	public String getReportName() {
		return reportName;
	}
	
	public abstract Component render(String componentId);
	
}
