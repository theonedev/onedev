package io.onedev.server.plugin.report.coverage;

public class NamedCoverageInfo extends CoverageInfo {

	private static final long serialVersionUID = 1L;
	
	private final String name;
	
	public NamedCoverageInfo(String name, 
							 int totalBranches, int coveredBranches,
							 int totalLines, int coveredLines) {
		super(totalBranches, coveredBranches, totalLines, coveredLines);
		this.name = name;
	}

	public NamedCoverageInfo(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
