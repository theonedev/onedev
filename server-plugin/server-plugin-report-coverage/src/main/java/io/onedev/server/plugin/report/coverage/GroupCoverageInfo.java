package io.onedev.server.plugin.report.coverage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupCoverageInfo extends NamedCoverageInfo {

	private static final long serialVersionUID = 1L;
	
	private List<FileCoverageInfo> fileCoverages;
	
	public GroupCoverageInfo(String name, int totalBranches, int coveredBranches,
							 int totalLines, int coveredLines,
							 List<FileCoverageInfo> fileCoverages) {
		super(name, totalBranches, coveredBranches, totalLines, coveredLines);
		this.fileCoverages = fileCoverages;
	}
	
	public GroupCoverageInfo(String name, CoverageInfo coverageInfo,
							 List<FileCoverageInfo> fileCoverages) {
		super(name, coverageInfo.getTotalBranches(), coverageInfo.getCoveredBranches(), 
				coverageInfo.getTotalLines(), coverageInfo.getCoveredLines());
		this.fileCoverages = fileCoverages;
	}
	
	public GroupCoverageInfo(String name) {
		this(name, 0, 0, 0, 0, new ArrayList<>());
	}
	
	public List<FileCoverageInfo> getFileCoverages() {
		return fileCoverages;
	}

	public void setFileCoverages(List<FileCoverageInfo> fileCoverages) {
		this.fileCoverages = fileCoverages;
	}

	@Override
	public void mergeWith(CoverageInfo otherCoverage) {
		super.mergeWith(otherCoverage);
		if (otherCoverage instanceof GroupCoverageInfo) {
			GroupCoverageInfo otherGroupCoverage = (GroupCoverageInfo) otherCoverage;
			Map<String, FileCoverageInfo> fileCoverageMap = new HashMap<>();
			for (var fileCoverage: fileCoverages)
				fileCoverageMap.put(fileCoverage.getBlobPath(), fileCoverage);
			for (var otherFileCoverage: otherGroupCoverage.fileCoverages) {
				var fileCoverage = fileCoverageMap.computeIfAbsent(otherFileCoverage.getBlobPath(), FileCoverageInfo::new);
				fileCoverage.mergeWith(otherFileCoverage);
			}
			fileCoverages = new ArrayList<>(fileCoverageMap.values());
		}
	}
}
