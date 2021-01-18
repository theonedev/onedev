package io.onedev.server.git;

import java.io.Serializable;

public class GitContribution implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Type {COMMITS, ADDITIONS, DELETIONS};
	
	private final int commits;
	
	private final int additions;
	
	private final int deletions;
	
	public GitContribution(int commits, int additions, int deletions) {
		this.commits = commits;
		this.additions = additions;
		this.deletions = deletions;
	}

	public int getCommits() {
		return commits;
	}

	public int getAdditions() {
		return additions;
	}

	public int getDeletions() {
		return deletions;
	}
	
}
