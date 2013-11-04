package com.pmease.commons.git;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

public class Commit {
    
    private final Date date;
    
    private final String author;
    
    private final String committer;
    
    private final String hash;
    
    private final String subject;
    
    private final String body;
    
    private final List<String> parentHashes;
    
    private final List<FileChange> fileChanges;

    public Commit(Date date, String author, String committer, String hash, String subject, 
    		String body, List<String> parentHashes, List<FileChange> fileChanges) {
    	this.date = date;
    	this.author = author;
    	this.committer = committer;
    	this.hash = hash;
    	this.subject = subject;
    	this.body = body;
    	this.parentHashes = new ArrayList<>(parentHashes);
    	this.fileChanges = new ArrayList<>(fileChanges);
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
