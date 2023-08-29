package io.onedev.server.plugin.report.coverage;

import static io.onedev.server.plugin.report.coverage.CoverageInfo.getCoverage;

public class TotalAndCoveredInfo {
	
	private final int totalStatements;

	private final int coveredStatements;
		
	private final int totalBranches;

	private final int coveredBranches;

	private final int totalLines;

	private final int coveredLines;

	private final int totalMethods;

	private final int coveredMethods;

	public TotalAndCoveredInfo(int totalStatements, int coveredStatements,
							   int totalMethods, int coveredMethods,
							   int totalBranches, int coveredBranches,
							   int totalLines, int coveredLines) {
		this.totalStatements = totalStatements;
		this.coveredStatements = coveredStatements;
		this.totalMethods = totalMethods;
		this.coveredMethods = coveredMethods;
		this.totalBranches = totalBranches;
		this.coveredBranches = coveredBranches;
		this.totalLines = totalLines;
		this.coveredLines = coveredLines;
	}

	public int getTotalStatements() {
		return totalStatements;
	}

	public int getCoveredStatements() {
		return coveredStatements;
	}

	public int getTotalMethods() {
		return totalMethods;
	}

	public int getCoveredMethods() {
		return coveredMethods;
	}

	public int getTotalBranches() {
		return totalBranches;
	}

	public int getCoveredBranches() {
		return coveredBranches;
	}

	public int getTotalLines() {
		return totalLines;
	}

	public int getCoveredLines() {
		return coveredLines;
	}

	public TotalAndCoveredInfo mergeWith(TotalAndCoveredInfo otherInfo) {
		return new TotalAndCoveredInfo(
				totalStatements + otherInfo.totalStatements, 
				coveredStatements + otherInfo.coveredStatements, 
				totalBranches + otherInfo.totalBranches, 
				coveredBranches + otherInfo.coveredBranches, 
				totalLines + otherInfo.totalLines,
				coveredLines + otherInfo.coveredLines,
				totalMethods + otherInfo.totalMethods,
				coveredMethods + otherInfo.coveredMethods);
	}
	
	public CoverageInfo getCoverageInfo() {
		return new CoverageInfo(
				getCoverage(totalStatements, coveredStatements),
				getCoverage(totalMethods, coveredMethods),
				getCoverage(totalBranches, coveredBranches), 
				getCoverage(totalLines, coveredLines));
	}
	
}
