package io.onedev.server.plugin.report.coverage;

public class FileCoverageInfo extends NamedCoverageInfo {

	private static final long serialVersionUID = 1L;
	
	private final String blobPath;
	
	public FileCoverageInfo(String name, 
			Coverage statementCoverage, Coverage methodCoverage, 
			Coverage branchCoverage, Coverage lineCoverage, 
			String blobPath) {
		super(name, statementCoverage, methodCoverage, branchCoverage, lineCoverage);
		this.blobPath = blobPath;
	}

	public FileCoverageInfo(String name, CoverageInfo coverageInfo, String blobPath) {
		super(name, coverageInfo.getStatementCoverage(), coverageInfo.getMethodCoverage(), 
				coverageInfo.getBranchCoverage(), coverageInfo.getLineCoverage());
		this.blobPath = blobPath;
	}
	
	public String getBlobPath() {
		return blobPath;
	}

}
