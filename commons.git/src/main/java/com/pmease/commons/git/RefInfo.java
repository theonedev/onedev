package com.pmease.commons.git;

public class RefInfo {
	
	private final String name;
	
	private final String commitHash;
	
	public RefInfo(final String name, final String commitHash) {
		this.name = name;
		this.commitHash = commitHash;
	}

	public String getName() {
		return name;
	}

	public String getCommitHash() {
		return commitHash;
	}
	
}
