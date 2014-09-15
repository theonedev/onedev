package com.pmease.gitplex.core.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.Lob;

import com.pmease.gitplex.core.comment.InlineContext;

@SuppressWarnings("serial")
@Embeddable
public class InlineInfo implements Serializable {

	private String commit;
	
	private String file;
	
	private Integer line;

	@Lob
	private InlineContext context;
	
	private String oldCommit;
	
	private String newCommit;

	public String getCommit() {
		return commit;
	}

	public void setCommit(String commit) {
		this.commit = commit;
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

	public String getOldCommit() {
		return oldCommit;
	}

	public void setOldCommit(String oldCommit) {
		this.oldCommit = oldCommit;
	}

	public String getNewCommit() {
		return newCommit;
	}

	public void setNewCommit(String newCommit) {
		this.newCommit = newCommit;
	}

}