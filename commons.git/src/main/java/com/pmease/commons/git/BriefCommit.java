package com.pmease.commons.git;

import java.util.Date;

import com.google.common.base.Objects;

public class BriefCommit {

    private final String hash;
    
    private final String committer;
    
    private final String committerEmail;
    
	private final Date committerDate;
    
    private final String author;
    
    private final String authorEmail;
    
    private final Date authorDate;
    
    private final String summary;

    public BriefCommit(String hash, String committer, String committerEmail, Date committerDate, 
    		String author, String authorEmail, Date authorDate, String summary) {
    	this.hash = hash;
    	this.committer = committer;
    	this.committerEmail = committerEmail;
    	this.committerDate = committerDate;
    	this.author = author;
    	this.authorEmail = authorEmail;
    	this.authorDate = authorDate;
    	this.summary = summary;
    }

	public String getHash() {
		return hash;
	}

	public String getCommitter() {
		return committer;
	}

	public String getCommitterEmail() {
		return committerEmail;
	}

	public Date getCommitterDate() {
		return committerDate;
	}

	public String getAuthor() {
		return author;
	}

	public String getAuthorEmail() {
		return authorEmail;
	}

	public Date getAuthorDate() {
		return authorDate;
	}

	public String getSummary() {
		return summary;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("hash", hash)
				.add("committer", committer)
				.add("date", committerDate)
				.add("summary", summary)
				.toString();
	}
    
}
