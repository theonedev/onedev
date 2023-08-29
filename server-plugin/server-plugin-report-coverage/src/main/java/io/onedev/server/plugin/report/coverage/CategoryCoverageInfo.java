package io.onedev.server.plugin.report.coverage;

import java.util.List;

public class CategoryCoverageInfo extends NamedCoverageInfo {

	private static final long serialVersionUID = 1L;
	
	private final List<ItemCoverageInfo> itemCoverages;
	
	public CategoryCoverageInfo(String name,
								int statementCoverage, int methodCoverage,
								int branchCoverage, int lineCoverage,
								List<ItemCoverageInfo> itemCoverages) {
		super(name, statementCoverage, methodCoverage, branchCoverage, lineCoverage);
		this.itemCoverages = itemCoverages;
	}

	public CategoryCoverageInfo(String name, CoverageInfo coverageInfo,
								List<ItemCoverageInfo> itemCoverages) {
		super(name, coverageInfo.getStatementCoverage(), coverageInfo.getMethodCoverage(), 
				coverageInfo.getBranchCoverage(), coverageInfo.getLineCoverage());
		this.itemCoverages = itemCoverages;
	}
	
	public List<ItemCoverageInfo> getItemCoverages() {
		return itemCoverages;
	}

}
