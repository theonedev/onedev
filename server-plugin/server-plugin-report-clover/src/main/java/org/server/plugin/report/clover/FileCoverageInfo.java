package org.server.plugin.report.clover;

import io.onedev.server.util.Coverage;

public class FileCoverageInfo extends NamedCoverageInfo {

	private static final long serialVersionUID = 1L;
	
	private final String path;
	
	public FileCoverageInfo(String name, 
			Coverage statementCoverage, Coverage methodCoverage, 
			Coverage branchCoverage, Coverage lineCoverage, 
			String path) {
		super(name, statementCoverage, methodCoverage, branchCoverage, lineCoverage);
		this.path = path;
	}

	public String getPath() {
		return path;
	}

}
