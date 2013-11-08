package com.pmease.commons.git;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

public class Commit {
    
    private final Date committerDate;
    
    private final Date authorDate;
    
    private final String author;
    
    private final String committer;
    
    private final String committerEmail;
    
    private final String authorEmail;
    
    private final String hash;
    
    private final String subject;
    
    private final String body;
    
    private final String note;
    
    private final List<String> parentHashes;
    
    private final List<FileChange> fileChanges;

    public Commit(Date committerDate, Date authorDate, String author, String committer,
    		String authorEmail, String committerEmail, String hash, String subject, 
    		@Nullable String body, String note, List<String> parentHashes, 
    		List<FileChange> fileChanges) {
    	this.committerDate = committerDate;
    	this.authorDate = authorDate;
    	this.author = author;
    	this.committer = committer;
    	this.authorEmail = authorEmail;
    	this.committerEmail = committerEmail;
    	this.hash = hash;
    	this.subject = subject;
    	this.body = body;
    	this.note = note;
    	this.parentHashes = new ArrayList<>(parentHashes);
    	this.fileChanges = new ArrayList<>(fileChanges);
    }
    
	public Date getCommitterDate() {
		return committerDate;
	}

	public Date getAuthorDate() {
		return authorDate;
	}

	public String getAuthor() {
		return author;
	}

	public String getCommitter() {
		return committer;
	}

	public String getCommitterEmail() {
		return committerEmail;
	}

	public String getAuthorEmail() {
		return authorEmail;
	}

	public String getHash() {
		return hash;
	}

	public String getSubject() {
		return subject;
	}

	public @Nullable String getBody() {
		return body;
	}

	public @Nullable String getNote() {
		return note;
	}

	public List<String> getParentHashes() {
		return Collections.unmodifiableList(parentHashes);
	}
	
	public List<FileChange> getFileChanges() {
		return Collections.unmodifiableList(fileChanges);
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("hash", getHash())
				.add("subject", getSubject())
				.toString();
	}
	
}
