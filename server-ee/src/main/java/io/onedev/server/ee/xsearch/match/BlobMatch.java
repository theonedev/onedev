package io.onedev.server.ee.xsearch.match;

import java.io.Serializable;
import java.util.List;

public class BlobMatch implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final Long projectId;
	
	private final String blobPath;
	
	private final List<ContentMatch> contentMatches;
	
	public BlobMatch(Long projectId, String blobPath, List<ContentMatch> contentMatches) {
		this.projectId = projectId;
		this.blobPath = blobPath;
		this.contentMatches = contentMatches;
	}

	public Long getProjectId() {
		return projectId;
	}

	public String getBlobPath() {
		return blobPath;
	}

	public List<ContentMatch> getContentMatches() {
		return contentMatches;
	}
	
}
