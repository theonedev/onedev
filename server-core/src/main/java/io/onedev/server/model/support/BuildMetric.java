package io.onedev.server.model.support;

import io.onedev.server.model.Build;

public interface BuildMetric {

	static final String PROP_BUILD = "build";
	
	static final String NAME_REPORT = "Report";
	
	static final String PROP_REPORT = "reportName";
	
	static final String FORMAT_PERCENTAGE = "function(value) {return value+'%';}";
	
	static final String FORMAT_DURATION = "function(seconds) {return onedev.server.formatBriefDuration(seconds);}";
	
	Build getBuild();
	
	String getReportName();
	
}
