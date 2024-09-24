package io.onedev.server.plugin.report.coverage;

import java.io.Serializable;

public class Coverage implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int totalBranches;

	private int coveredBranches;

	private int totalLines;

	private int coveredLines;

	public Coverage(int totalBranches, int coveredBranches,
					int totalLines, int coveredLines) {
		this.totalBranches = totalBranches;
		this.coveredBranches = coveredBranches;
		this.totalLines = totalLines;
		this.coveredLines = coveredLines;
	}

	public Coverage() {
		this(0, 0, 0, 0);
	}
	
	public int getTotalBranches() {
		return totalBranches;
	}

	public int getCoveredBranches() {
		return coveredBranches;
	}
	
	public int getBranchPercentage() {
		return getPercentage(totalBranches, coveredBranches);
	}

	public int getTotalLines() {
		return totalLines;
	}

	public int getCoveredLines() {
		return coveredLines;
	}
	
	public int getLinePercentage() {
		return getPercentage(totalLines, coveredLines);
	}

	public void setTotalBranches(int totalBranches) {
		this.totalBranches = totalBranches;
	}

	public void setCoveredBranches(int coveredBranches) {
		this.coveredBranches = coveredBranches;
	}

	public void setTotalLines(int totalLines) {
		this.totalLines = totalLines;
	}

	public void setCoveredLines(int coveredLines) {
		this.coveredLines = coveredLines;
	}

	public void mergeWith(Coverage otherCoverage) {
		totalBranches += otherCoverage.totalBranches;
		coveredBranches += otherCoverage.coveredBranches;
		totalLines += otherCoverage.totalLines;
		coveredLines += otherCoverage.coveredLines;
	}
	
	public static int getPercentage(int total, int covered)	{
		return total != 0? covered * 100 / total: -1;
	}
	
}
