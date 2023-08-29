package io.onedev.server.plugin.report.coverage;

import javax.annotation.Nullable;
import javax.validation.constraints.Null;
import java.io.Serializable;

public class CoverageInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final int statementCoverage;
	
	private final int methodCoverage;
	
	private final int branchCoverage;
	
	private final int lineCoverage;
	
	public CoverageInfo(int statementCoverage, int methodCoverage,
						int branchCoverage, int lineCoverage) {
		this.statementCoverage = statementCoverage;
		this.methodCoverage = methodCoverage;
		this.branchCoverage = branchCoverage;
		this.lineCoverage = lineCoverage;
	}

	public int getStatementCoverage() {
		return statementCoverage;
	}

	public int getMethodCoverage() {
		return methodCoverage;
	}

	public int getBranchCoverage() {
		return branchCoverage;
	}

	public int getLineCoverage() {
		return lineCoverage;
	}
	
	public static int getCoverage(int total, int covered) {
		return total != 0? covered * 100 / total: 0;
	}
	
}
