package io.onedev.server.plugin.report.coverage;

public class FileCoverageInfo extends NamedCoverageInfo {

	private static final long serialVersionUID = 1L;
	
	public FileCoverageInfo(String blobPath, int totalBranches, int coveredBranches,
							int totalLines, int coveredLines) {
		super(blobPath, totalBranches, coveredBranches, totalLines, coveredLines);
	}

	public FileCoverageInfo(String blobPath, CoverageInfo coverageInfo) {
		this(blobPath, coverageInfo.getTotalBranches(), coverageInfo.getCoveredBranches(), 
				coverageInfo.getTotalLines(), coverageInfo.getCoveredLines());
	}
	
	public FileCoverageInfo(String blobPath) {
		super(blobPath);
	}
	
	public String getBlobPath() {
		return getName();
	}

}
