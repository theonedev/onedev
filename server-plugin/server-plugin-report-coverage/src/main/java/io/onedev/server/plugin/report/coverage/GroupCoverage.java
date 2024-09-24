package io.onedev.server.plugin.report.coverage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupCoverage extends NamedCoverage {

	private static final long serialVersionUID = 1L;
	
	private List<FileCoverage> fileCoverages;
	
	public GroupCoverage(String name, int totalBranches, int coveredBranches,
						 int totalLines, int coveredLines,
						 List<FileCoverage> fileCoverages) {
		super(name, totalBranches, coveredBranches, totalLines, coveredLines);
		this.fileCoverages = fileCoverages;
	}
	
	public GroupCoverage(String name, Coverage coverage,
						 List<FileCoverage> fileCoverages) {
		super(name, coverage.getTotalBranches(), coverage.getCoveredBranches(), 
				coverage.getTotalLines(), coverage.getCoveredLines());
		this.fileCoverages = fileCoverages;
	}
	
	public GroupCoverage(String name) {
		this(name, 0, 0, 0, 0, new ArrayList<>());
	}
	
	public List<FileCoverage> getFileCoverages() {
		return fileCoverages;
	}

	public void setFileCoverages(List<FileCoverage> fileCoverages) {
		this.fileCoverages = fileCoverages;
	}

	@Override
	public void mergeWith(Coverage otherCoverage) {
		super.mergeWith(otherCoverage);
		if (otherCoverage instanceof GroupCoverage) {
			GroupCoverage otherGroupCoverage = (GroupCoverage) otherCoverage;
			Map<String, FileCoverage> fileCoverageMap = new HashMap<>();
			for (var fileCoverage: fileCoverages)
				fileCoverageMap.put(fileCoverage.getBlobPath(), fileCoverage);
			for (var otherFileCoverage: otherGroupCoverage.fileCoverages) {
				var fileCoverage = fileCoverageMap.computeIfAbsent(otherFileCoverage.getBlobPath(), FileCoverage::new);
				fileCoverage.mergeWith(otherFileCoverage);
			}
			fileCoverages = new ArrayList<>(fileCoverageMap.values());
		}
	}
}
