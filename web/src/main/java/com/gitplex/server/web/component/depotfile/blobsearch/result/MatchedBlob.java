package com.gitplex.server.web.component.depotfile.blobsearch.result;

import java.io.Serializable;
import java.util.List;

import com.gitplex.server.search.hit.QueryHit;
import com.gitplex.jsymbol.Range;

public class MatchedBlob implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String blobPath;
	
	private final List<QueryHit> hits;
	
	private Range matchRange;
	
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

	public Range getMatchRange() {
		return matchRange;
	}

	public void setMatchRange(Range matchRange) {
		this.matchRange = matchRange;
	}

}
