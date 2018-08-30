package io.onedev.server.web.page.project.blob.search.result;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.utils.Range;

public class MatchedBlob implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String blobPath;
	
	private final List<QueryHit> hits;
	
	private Range match;
	
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

	public Range getMatch() {
		return match;
	}

	public void setMatch(Range match) {
		this.match = match;
	}

}
