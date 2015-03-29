package com.pmease.gitplex.search.hit;

import java.util.List;

public class PathHits {
	
	private final String blobPath;
	
	private final List<QueryHit> hits;
	
	public PathHits(String blobPath, List<QueryHit> hits) {
		this.blobPath = blobPath;
		this.hits = hits;
	}

	public String getBlobPath() {
		return blobPath;
	}

	public List<QueryHit> getHits() {
		return hits;
	}

}
