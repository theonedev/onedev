package com.pmease.commons.git;

import com.google.common.base.Objects;

public class BriefCommit {

    private final String hash;
    
    private final UserInfo committer;
    
    private final UserInfo author;
    
    private final String summary;

    public BriefCommit(String hash, UserInfo committer, UserInfo author, String summary) {
    	this.hash = hash;
    	this.committer = committer;
    	this.author = author;
    	this.summary = summary;
    }

	public String getHash() {
		return hash;
	}

	public UserInfo getCommitter() {
		return committer;
	}

	public UserInfo getAuthor() {
		return author;
	}

	public String getSummary() {
		return summary;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("hash", hash)
				.add("committer", committer.getName())
				.add("date", committer.getDate())
				.add("summary", summary)
				.toString();
	}
    
}
