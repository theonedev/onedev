package com.pmease.gitplex.search.hit;

public abstract class QueryHit {
	
	private final String blobPath;
	
	public QueryHit(String blobPath) {
		this.blobPath = blobPath;
	}

	public String getBlobPath() {
		return blobPath;
	}
	
}
