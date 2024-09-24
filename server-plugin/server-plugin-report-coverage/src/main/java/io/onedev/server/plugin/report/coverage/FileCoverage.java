package io.onedev.server.plugin.report.coverage;

public class FileCoverage extends NamedCoverage {

	private static final long serialVersionUID = 1L;
	
	public FileCoverage(String blobPath, int totalBranches, int coveredBranches,
						int totalLines, int coveredLines) {
		super(blobPath, totalBranches, coveredBranches, totalLines, coveredLines);
	}

	public FileCoverage(String blobPath, Coverage coverageInfo) {
		this(blobPath, coverageInfo.getTotalBranches(), coverageInfo.getCoveredBranches(), 
				coverageInfo.getTotalLines(), coverageInfo.getCoveredLines());
	}
	
	public FileCoverage(String blobPath) {
		super(blobPath);
	}
	
	public String getBlobPath() {
		return getName();
	}

}
