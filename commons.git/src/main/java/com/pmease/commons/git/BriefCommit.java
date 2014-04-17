package com.pmease.commons.git;

import java.io.Serializable;
import java.util.Date;

import com.google.common.base.Objects;

@SuppressWarnings("serial")
public class BriefCommit implements Serializable {

    private final String hash;
    
    private final GitIdentity committer;
    
    private final Date commitDate;
    
    private final GitIdentity author;
    
    private final Date authorDate;
    
    private final String subject;

    public BriefCommit(String hash, GitIdentity committer, Date commitDate, 
    		GitIdentity author, Date authorDate, String subject) {
    	this.hash = hash;
    	this.committer = committer;
    	this.commitDate = commitDate;
    	this.author = author;
    	this.authorDate = authorDate;
    	this.subject = subject;
    }

	public String getHash() {
		return hash;
	}

	public GitIdentity getCommitter() {
		return committer;
	}

	public GitIdentity getAuthor() {
		return author;
	}
	
	public Date getCommitDate() {
		return commitDate;
	}
	
	public Date getAuthorDate() {
		return authorDate;
	}

	public String getSubject() {
		return subject;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(hash);
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof BriefCommit)) return false;
		
		BriefCommit rhs = (BriefCommit) other;
		return Objects.equal(hash, rhs.hash);
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("hash", hash)
				.add("committer", committer.getName())
				.add("commit date", commitDate)
				.add("subject", subject)
				.toString();
	}
    
}
