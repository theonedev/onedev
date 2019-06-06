package io.onedev.server.git;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Date;

import org.eclipse.jgit.lib.PersonIdent;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class BlameCommit implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String hash;
    
    private final String subject;
    
    private final PersonIdent committer;
    
    private final PersonIdent author;
    
    public BlameCommit(String hash, PersonIdent committer, PersonIdent author, String subject) {
    	this.hash = checkNotNull(hash, "hash");
    	this.committer = checkNotNull(committer, "committer");
    	this.author = checkNotNull(author, "author");
    	this.subject = checkNotNull(subject, "subject");
    }

    public BlameCommit(BlameCommit commit) {
    	this(commit.getHash(), commit.getCommitter(), commit.getAuthor(), commit.getSubject());
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
		if (!(other instanceof BlameCommit)) return false;
		
		BlameCommit rhs = (BlameCommit) other;
		return Objects.equal(hash, rhs.hash);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("hash", hash)
				.add("committer", committer.getName())
				.add("date", committer.getWhen())
				.add("messageSummary", subject)
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
        
        public String subject;
		
		public BlameCommit build() {
			PersonIdent committer = GitUtils.newPersonIdent(committerName, committerEmail, committerDate);
			PersonIdent author = GitUtils.newPersonIdent(authorName, authorEmail, authorDate);
			return new BlameCommit(hash, committer, author, subject.trim());
		}
    }
}
