package com.gitplex.server.git.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.PersonIdent;

import com.gitplex.server.git.GitUtils;

public class LogCommit implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String hash;
	
	private final Date commitDate;
    
    private final PersonIdent committer;
    
    private final PersonIdent author;

    private final List<String> parentHashes;
    
    private final List<FileChange> fileChanges;
    
    public LogCommit(String hash, @Nullable Date commitDate, @Nullable PersonIdent committer, 
    		@Nullable PersonIdent author, List<String> parentHashes, List<FileChange> fileChanges, 
    		int addedLines, int deletedLines) {
    	this.hash = hash;
    	this.commitDate = commitDate;
    	this.committer = committer;
    	this.author = author;
    	this.parentHashes = new ArrayList<>(parentHashes);
    	this.fileChanges = new ArrayList<>(fileChanges);
    }
    
	public List<String> getParentHashes() {
		return parentHashes;
	}
	
    public List<FileChange> getFileChanges() {
		return fileChanges;
	}

    public Collection<String> getChangedFiles() {
    	Collection<String> changedFiles = new HashSet<>();
    	for (FileChange change: getFileChanges()) {
    		if (change.getPath() != null)
    			changedFiles.add(change.getPath());
    		if (change.getOldPath() != null)
    			changedFiles.add(change.getOldPath());
    	}
    	return changedFiles;
    }
    
	public String getHash() {
		return hash;
	}

	@Nullable 
	public PersonIdent getCommitter() {
		return committer;
	}

	@Nullable
	public PersonIdent getAuthor() {
		return author;
	}

	public Date getCommitDate() {
		return commitDate;
	}

	public int getAdditions() {
		int additions = 0;
		for (FileChange change: fileChanges)
			additions += change.getAdditions();
		return additions;
	}
	
	public int getDeletions() {
		int deletions = 0;
		for (FileChange change: fileChanges)
			deletions += change.getDeletions();
		return deletions;
	}	
	
	public static class Builder {

		public String hash;
		
		public String authorName;
		
		public String authorEmail;
		
		public Date authorDate;
		
		public String committerName;
		
		public String committerEmail;
		
		public Date committerDate;
		
    	public List<String> parentHashes = new ArrayList<>();
		
    	public List<FileChange> fileChanges = new ArrayList<>();
    	
    	public int addedLines;
    	
    	public int deletedLines;
		
		public LogCommit build() {
			PersonIdent committer;
			if (StringUtils.isNotBlank(committerName) || StringUtils.isNotBlank(committerEmail))
				committer = GitUtils.newPersonIdent(committerName, committerEmail, committerDate);
			else 
				committer = null;

			PersonIdent author;
			if (StringUtils.isNotBlank(authorName) || StringUtils.isNotBlank(authorEmail)) 
				author = GitUtils.newPersonIdent(authorName, authorEmail, authorDate);
			else
				author = null;
			
			return new LogCommit(hash, committerDate, committer, author, parentHashes, fileChanges, 
					addedLines, deletedLines);
		}
	}
	
}
