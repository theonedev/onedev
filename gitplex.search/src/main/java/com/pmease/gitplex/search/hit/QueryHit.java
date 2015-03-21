package com.pmease.gitplex.search.hit;

import org.apache.wicket.Component;

public abstract class QueryHit {
	
	private final String blobPath;
	
	public QueryHit(String blobPath) {
		this.blobPath = blobPath;
	}

	public String getBlobPath() {
		return blobPath;
	}
	
	public abstract Component render(String componentId);
	
}
