package org.server.plugin.report.clover;

import io.onedev.server.util.Coverage;

public class NamedCoverageInfo extends CoverageInfo {

	private static final long serialVersionUID = 1L;
	
	private final String name;
	
	public NamedCoverageInfo(String name, 
			Coverage statementCoverage, Coverage methodCoverage, 
			Coverage branchCoverage, Coverage lineCoverage) {
		super(statementCoverage, methodCoverage, branchCoverage, lineCoverage);
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
}
