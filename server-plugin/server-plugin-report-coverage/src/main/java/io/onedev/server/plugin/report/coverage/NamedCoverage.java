package io.onedev.server.plugin.report.coverage;

public class NamedCoverage extends Coverage {

	private static final long serialVersionUID = 1L;
	
	private final String name;
	
	public NamedCoverage(String name,
						 int totalBranches, int coveredBranches,
						 int totalLines, int coveredLines) {
		super(totalBranches, coveredBranches, totalLines, coveredLines);
		this.name = name;
	}

	public NamedCoverage(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
