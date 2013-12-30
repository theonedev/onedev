package com.pmease.gitop.web;

import java.io.Serializable;

public class SessionData implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long accountId;
	private Long projectId;
	private String revision;

	public static SessionData get() {
		return GitopSession.get().getSessionData();
	}
	
	public void onAccountChanged() {
		this.projectId = null;
		this.revision = null;
	}
	
	public void onProjectChanged() {
		this.revision = null;
	}
	
	public void onRevisionChanged() {
	}
	
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
}
