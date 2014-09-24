package com.pmease.gitplex.core.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.Lob;

import com.pmease.gitplex.core.comment.InlineContext;

@SuppressWarnings("serial")
@Embeddable
public class InlineInfo implements Serializable {

	private String commitHash;
	
	private String file;
	
	private Integer line;

	@Lob
	private InlineContext context;
	
	private String oldCommitHash;
	
	private String newCommitHash;

	public String getCommitHash() {
		return commitHash;
	}

	public void setCommitHash(String commitHash) {
		this.commitHash = commitHash;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public InlineContext getContext() {
		return context;
	}

	public void setContext(InlineContext context) {
		this.context = context;
	}

	public String getOldCommitHash() {
		return oldCommitHash;
	}

	public void setOldCommitHash(String oldCommitHash) {
		this.oldCommitHash = oldCommitHash;
	}

	public String getNewCommitHash() {
		return newCommitHash;
	}

	public void setNewCommitHash(String newCommitHash) {
		this.newCommitHash = newCommitHash;
	}

}