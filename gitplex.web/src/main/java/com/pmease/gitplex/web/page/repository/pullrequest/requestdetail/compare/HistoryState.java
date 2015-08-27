package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.compare;

import java.io.Serializable;

class HistoryState implements Serializable {

	private static final long serialVersionUID = 1L;

	public String oldCommitHash;
	
	public String newCommitHash;
	
	public String path;
	
	public String comparePath;
	
	public Long commentId;
}
