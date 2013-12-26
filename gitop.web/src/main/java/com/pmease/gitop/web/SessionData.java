package com.pmease.gitop.web;

import java.io.Serializable;
import java.util.List;

public class SessionData implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long accountId;
	private Long projectId;
	private String revision;
	private List<String> paths;

	
	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public List<String> getPaths() {
		return paths;
	}

	public void setPaths(List<String> paths) {
		this.paths = paths;
	}

}
