package org.server.plugin.report.clover;

import java.util.List;

import io.onedev.server.util.Coverage;

public class PackageCoverageInfo extends NamedCoverageInfo {

	private static final long serialVersionUID = 1L;
	
	private final List<FileCoverageInfo> fileCoverages;
	
	public PackageCoverageInfo(String name, 
			Coverage statementCoverage, Coverage methodCoverage, 
			Coverage branchCoverage, Coverage lineCoverage, 
			List<FileCoverageInfo> fileCoverages) {
		super(name, statementCoverage, methodCoverage, branchCoverage, lineCoverage);
		this.fileCoverages = fileCoverages;
	}

	public List<FileCoverageInfo> getFileCoverages() {
		return fileCoverages;
	}

}
