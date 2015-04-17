package com.pmease.commons.git;

import java.io.Serializable;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

public class LastCommitInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final ObjectId id;
	
	private final String summary;
	
	private final long timestamp;
	
	public LastCommitInfo(RevCommit commit) {
		this.id = commit.getId();
		this.summary = commit.getShortMessage();
		this.timestamp = commit.getCommitTime();
	}

	public ObjectId getId() {
		return id;
	}

	public String getSummary() {
		return summary;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
}