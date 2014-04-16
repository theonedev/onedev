package com.pmease.gitop.web;

import java.io.Serializable;

public class SessionData implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long accountId;
	private Long repositoryId;
	private String revision;

	public static SessionData get() {
		return GitopSession.get().getSessionData();
	}
	
	public void onAccountChanged() {
		this.repositoryId = null;
		this.revision = null;
	}
	
	public void onRepositoryChanged() {
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

	public Long getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(Long repositoryId) {
		this.repositoryId = repositoryId;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}
}
