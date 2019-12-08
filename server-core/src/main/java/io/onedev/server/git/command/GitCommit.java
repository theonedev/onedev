package io.onedev.server.git.command;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.PersonIdent;

import io.onedev.server.git.GitUtils;

public class GitCommit implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String hash;
	
	private final String subject;
	
	private final String body;
	
	private final Date commitDate;
    
    private final PersonIdent committer;
    
    private final PersonIdent author;

    private final List<String> parentHashes;
    
    private final List<FileChange> fileChanges;
    
    public GitCommit(String hash, @Nullable List<String> parentHashes, @Nullable PersonIdent committer, 
    		@Nullable PersonIdent author, @Nullable Date commitDate, @Nullable String subject, 
    		@Nullable String body, @Nullable List<FileChange> fileChanges) {
    	this.hash = hash;
    	this.parentHashes = parentHashes;
    	this.committer = committer;
    	this.author = author;
    	this.commitDate = commitDate;
    	this.subject = subject;
    	this.body = body;
    	this.fileChanges = fileChanges;
    }

    @Nullable
	public String getSubject() {
		return subject;
	}

    @Nullable
	public String getBody() {
		return body;
	}

    @Nullable
	public List<String> getParentHashes() {
		return parentHashes;
	}
	
    @Nullable
    public List<FileChange> getFileChanges() {
		return fileChanges;
	}

	public Collection<String> getChangedFiles() {
    	Collection<String> changedFiles = new HashSet<>();
    	for (FileChange change: getFileChanges()) {
    		if (change.getNewPath() != null)
    			changedFiles.add(change.getNewPath());
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

	@Nullable
	public Date getCommitDate() {
		return commitDate;
	}

	public int getAdditions() {
		int additions = 0;
		for (FileChange change: fileChanges) {
			if (change.getAdditions() > 0)
				additions += change.getAdditions();
		}
		return additions;
	}
	
	public int getDeletions() {
		int deletions = 0;
		for (FileChange change: fileChanges) {
			if (change.getDeletions() > 0)
			deletions += change.getDeletions();
		}
		return deletions;
	}	
	
	public static class Builder {

		public String hash;
		
		public String subject;
		
		public String body;
		
		public String authorName;
		
		public String authorEmail;
		
		public Date authorDate;
		
		public String committerName;
		
		public String committerEmail;
		
		public Date committerDate;
		
		public Date commitDate;
		
    	public List<String> parentHashes;
		
    	public List<FileChange> fileChanges;
    	
		public GitCommit build() {
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

			if (subject != null)
				subject = subject.trim();
			
			if (body != null) 
				body = body.trim();
			
			return new GitCommit(hash, parentHashes, committer, author, commitDate, 
					subject, body, fileChanges);
		}
	}
	
}
