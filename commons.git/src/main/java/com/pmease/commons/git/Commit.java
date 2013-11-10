package com.pmease.commons.git;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class Commit extends BriefCommit {
    
	public static final String ZERO_HASH = "0000000000000000000000000000000000000000"; 
	
    private final String message;
    
    private final String note;
    
    private final List<String> parentHashes;
    
    private final List<FileChange> fileChanges;

    public Commit(String hash, String committer, String committerEmail, 
    		Date committerDate, String author, String authorEmail, 
    		Date authorDate, String summary, @Nullable String message, 
    		@Nullable String note, List<String> parentHashes, 
    		List<FileChange> fileChanges) {
    	super(hash, committer, committerEmail, committerDate, author, 
    			authorEmail, authorDate, summary);
    	
    	this.message = message;
    	this.note = note;
    	this.parentHashes = new ArrayList<>(parentHashes);
    	this.fileChanges = new ArrayList<>(fileChanges);
    }
    
	public @Nullable String getMessage() {
		return message;
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
	
}
