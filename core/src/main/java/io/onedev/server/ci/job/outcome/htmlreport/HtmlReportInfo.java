package io.onedev.server.ci.job.outcome.htmlreport;

import java.io.Serializable;

public class HtmlReportInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String reportName;
	
	private final String startPage;
	
	public HtmlReportInfo(String reportName, String startPage) {
		this.reportName = reportName;
		this.startPage = startPage;
	}

	public String getReportName() {
		return reportName;
	}

	public String getStartPage() {
		return startPage;
	}
	
}