package com.pmease.commons.git;

import java.io.Serializable;

import org.eclipse.jgit.lib.PersonIdent;

import com.google.common.base.Objects;

@SuppressWarnings("serial")
public class BriefCommit implements Serializable {

    private final String hash;
    
    private final PersonIdent committer;
    
    private final PersonIdent author;
    
    private final String subject;

    public BriefCommit(String hash, PersonIdent committer, PersonIdent author, String subject) {
    	this.hash = hash;
    	this.committer = committer;
    	this.author = author;
    	this.subject = subject;
    }

	public String getHash() {
		return hash;
	}

	public PersonIdent getCommitter() {
		return committer;
	}

	public PersonIdent getAuthor() {
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
				.add("committer", committer.getName())
				.add("date", committer.getWhen())
				.add("subject", subject)
				.toString();
	}
    
}
