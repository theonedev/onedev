package io.onedev.server.plugin.report.coverage;

import java.util.List;

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

	public PackageCoverageInfo(String name, CoverageInfo coverageInfo,
			List<FileCoverageInfo> fileCoverages) {
		super(name, coverageInfo.getStatementCoverage(), coverageInfo.getMethodCoverage(), 
				coverageInfo.getBranchCoverage(), coverageInfo.getLineCoverage());
		this.fileCoverages = fileCoverages;
	}
	
	public List<FileCoverageInfo> getFileCoverages() {
		return fileCoverages;
	}

}
