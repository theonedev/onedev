package io.onedev.server.plugin.report.coverage;

import io.onedev.server.codequality.CoverageStatus;

import java.util.Map;

public class ProcessResult {
	
	private final CoverageReport report;
	
	private final Map<String, Map<Integer, CoverageStatus>> statuses;
	
	public ProcessResult(CoverageReport report, Map<String, Map<Integer, CoverageStatus>> statuses) {
		this.report = report;
		this.statuses = statuses;
	}

	public CoverageReport getReport() {
		return report;
	}

	public Map<String, Map<Integer, CoverageStatus>> getStatuses() {
		return statuses;
	}
}
