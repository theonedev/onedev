package io.onedev.server.plugin.report.coverage;

import java.io.Serializable;

public class CoverageInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final Coverage statementCoverage;
	
	private final Coverage methodCoverage;
	
	private final Coverage branchCoverage;
	
	private final Coverage lineCoverage;
	
	public CoverageInfo(
			Coverage statementCoverage, Coverage methodCoverage, 
			Coverage branchCoverage, Coverage lineCoverage) {
		this.statementCoverage = statementCoverage;
		this.methodCoverage = methodCoverage;
		this.branchCoverage = branchCoverage;
		this.lineCoverage = lineCoverage;
	}

	public Coverage getStatementCoverage() {
		return statementCoverage;
	}

	public Coverage getMethodCoverage() {
		return methodCoverage;
	}

	public Coverage getBranchCoverage() {
		return branchCoverage;
	}

	public Coverage getLineCoverage() {
		return lineCoverage;
	}
	
	public CoverageInfo mergeWith(CoverageInfo coverageInfo) {
		return new CoverageInfo(
				statementCoverage.mergeWith(coverageInfo.statementCoverage), 
				methodCoverage.mergeWith(coverageInfo.methodCoverage), 
				branchCoverage.mergeWith(coverageInfo.branchCoverage), 
				lineCoverage.mergeWith(coverageInfo.lineCoverage));
	}
	
}
