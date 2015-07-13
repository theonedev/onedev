package com.pmease.gitplex.web.component.repofile.blobsearch.result;

import java.io.Serializable;
import java.util.List;

import com.pmease.gitplex.search.hit.QueryHit;

public class MatchedBlob implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String blobPath;
	
	private final List<QueryHit> hits;
	
	public MatchedBlob(String blobPath, List<QueryHit> hits) {
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
