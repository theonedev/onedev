package com.pmease.commons.git;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;

import com.google.common.base.Objects;

@SuppressWarnings("serial")
public class BriefCommit implements Serializable {

    private final String hash;
    
    private final GitContrib committer;
    
    private final GitContrib author;
    
    private final String subject;

    public BriefCommit(String hash, GitContrib committer, GitContrib author, String subject) {
    	this.hash = checkNotNull(hash, "hash");
    	this.committer = checkNotNull(committer, "committer");
    	this.author = checkNotNull(author, "author");
    	this.subject = checkNotNull(subject, "subject");
    }

	public String getHash() {
		return hash;
	}

	public GitContrib getCommitter() {
		return committer;
	}

	public GitContrib getAuthor() {
		return author;
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
				.add("committer", committer.getPerson().getName())
				.add("date", committer.getDate())
				.add("subject", subject)
				.toString();
	}
    
}
