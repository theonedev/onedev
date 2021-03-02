package org.server.plugin.report.clover;

import java.io.Serializable;

import io.onedev.server.util.Coverage;

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
	
}
