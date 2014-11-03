package com.pmease.commons.git;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Date;

import org.eclipse.jgit.lib.PersonIdent;

import com.google.common.base.Objects;

@SuppressWarnings("serial")
public class BriefCommit implements Serializable {

    private final String hash;
    
    private final String messageSummary;
    
    private final PersonIdent committer;
    
    private final PersonIdent author;
    
    public BriefCommit(String hash, PersonIdent committer, PersonIdent author, String messageSummary) {
    	this.hash = checkNotNull(hash, "hash");
    	this.committer = checkNotNull(committer, "committer");
    	this.author = checkNotNull(author, "author");
    	this.messageSummary = checkNotNull(messageSummary, "messageSummary");
    }

    public BriefCommit(BriefCommit commit) {
    	this(commit.getHash(), commit.getCommitter(), commit.getAuthor(), commit.getMessageSummary());
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

	public String getMessageSummary() {
		return messageSummary;
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
				.add("messageSummary", messageSummary)
				.toString();
	}

    public static class Builder {
    	
		public String hash;
		
        public String committerName;
        
        public String committerEmail;
		
        public Date committerDate;
    	
        public String authorName;
        
        public Date authorDate;
        
        public String authorEmail;
        
        public String messageSummary;
		
		public BriefCommit build() {
			PersonIdent committer = GitUtils.newPersonIdent(committerName, committerEmail, committerDate);
			PersonIdent author = GitUtils.newPersonIdent(authorName, authorEmail, authorDate);
			return new BriefCommit(hash, committer, author, messageSummary.trim());
		}
    }
}
