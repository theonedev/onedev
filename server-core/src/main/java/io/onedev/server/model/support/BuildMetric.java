package io.onedev.server.model.support;

import io.onedev.server.model.Build;

public interface BuildMetric {

	static final String PROP_BUILD = "build";
	
	static final String NAME_REPORT = "Report";
	
	static final String PROP_REPORT = "reportName";
	
	Build getBuild();
	
	String getReportName();
	
}
