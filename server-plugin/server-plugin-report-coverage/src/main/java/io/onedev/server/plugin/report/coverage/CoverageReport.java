package io.onedev.server.plugin.report.coverage;

import io.onedev.server.codequality.CoverageStatus;

import java.util.Map;

public class CoverageReport {
	
	private final CoverageStats stats;
	
	private final Map<String, Map<Integer, CoverageStatus>> statuses;
	
	public CoverageReport(CoverageStats stats, Map<String, Map<Integer, CoverageStatus>> statuses) {
		this.stats = stats;
		this.statuses = statuses;
	}

	public CoverageStats getStats() {
		return stats;
	}

	public Map<String, Map<Integer, CoverageStatus>> getStatuses() {
		return statuses;
	}
	
}
