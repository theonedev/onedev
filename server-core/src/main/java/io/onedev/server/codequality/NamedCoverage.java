package io.onedev.server.codequality;

import java.io.Serial;

public class NamedCoverage extends Coverage {

	@Serial
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
