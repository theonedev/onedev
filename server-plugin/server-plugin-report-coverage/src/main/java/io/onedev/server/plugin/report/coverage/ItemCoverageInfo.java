package io.onedev.server.plugin.report.coverage;

public class ItemCoverageInfo extends NamedCoverageInfo {

	private static final long serialVersionUID = 1L;
	
	private final String blobPath;
	
	public ItemCoverageInfo(String name,
							int statementCoverage, int methodCoverage,
							int branchCoverage, int lineCoverage,
							String blobPath) {
		super(name, statementCoverage, methodCoverage, branchCoverage, lineCoverage);
		this.blobPath = blobPath;
	}

	public ItemCoverageInfo(String name, CoverageInfo coverageInfo, String blobPath) {
		super(name, coverageInfo.getStatementCoverage(), coverageInfo.getMethodCoverage(), 
				coverageInfo.getBranchCoverage(), coverageInfo.getLineCoverage());
		this.blobPath = blobPath;
	}
	
	public String getBlobPath() {
		return blobPath;
	}

}
