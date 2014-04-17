package com.pmease.commons.git;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class Commit extends BriefCommit {
    
	public static final String ZERO_HASH = "0000000000000000000000000000000000000000"; 
	
    private final String body;
    
    private final String note;
    
    private final List<String> parentHashes;
    
    private final List<FileChange> fileChanges;

    public static class Builder {
		String hash;
		String subject;
		String body;
		String note;
		GitContribInfo author;
		GitContribInfo committer;
		List<String> parents = Lists.newArrayList();
		List<FileChange> changes = Lists.newArrayList();
		
		public Builder hash(String hash) {
			this.hash = hash;
			return this;
		}
		
		public Builder subject(String subject) {
			this.subject = subject;
			return this;
		}
		
		public Builder body(String body) {
			if (!StringUtils.isBlank(body))
				this.body = body;
			
			return this;
		}
		
		public Builder note(String note) {
			this.note = note;
			return this;
		}
		
		public Builder author(GitContribInfo author) {
			this.author = author;
			return this;
		}
		
		public Builder committer(GitContribInfo committer) {
			this.committer = committer;
			return this;
		}
		
		public Builder parents(List<String> parents) {
			this.parents = parents;
			return this;
		}
		
		public Builder changes(List<FileChange> changes) {
			this.changes = changes;
			return this;
		}
		
		public Commit build() {
			return new Commit(
					checkNotNull(hash, "hash"), 
					checkNotNull(committer, "committer"), 
					checkNotNull(author, "author"), 
					checkNotNull(subject, "subject"), 
					body, note, 
					checkNotNull(parents, "parents"), 
					checkNotNull(changes, "changes"));
		}
	}
	
    public static Builder builder() {
    	return new Builder();
    }
    
    public Commit(String hash, GitContribInfo committer, GitContribInfo author, 
    		String subject, @Nullable String body, 
    		@Nullable String note, List<String> parentHashes, 
    		List<FileChange> fileChanges) {
    	super(hash, committer, author, subject);
    	
    	this.body = body;
    	this.note = note;
    	this.parentHashes = new ArrayList<>(parentHashes);
    	this.fileChanges = new ArrayList<>(fileChanges);
    }
    
	public String getMessage() {
		return Strings.isNullOrEmpty(body) ? getSubject() : getSubject() + "\n" + body;
	}

	public @Nullable String getMessageBody() {
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

}
