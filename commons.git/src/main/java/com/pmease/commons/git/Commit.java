package com.pmease.commons.git;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import javax.annotation.Nullable;

public class Commit {
    
    private final Date date;
    
    private final String author;
    
    private final String committer;
    
    private final String hash;
    
    private final String subject;
    
    private final String body;
    
    private final Collection<String> parentHashes;

    public Commit(Date date, String author, String committer, String hash, String subject, 
    		String body, Collection<String> parentHashes) {
    	this.date = date;
    	this.author = author;
    	this.committer = committer;
    	this.hash = hash;
    	this.subject = subject;
    	this.body = body;
    	this.parentHashes = new HashSet<>(parentHashes);
    }
    
    public Date getDate() {
        return date;
    }

    public String getAuthor() {
        return author;
    }

    public String getCommitter() {
        return committer;
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

	public Collection<String> getParentHashes() {
		return Collections.unmodifiableCollection(parentHashes);
	}
}
