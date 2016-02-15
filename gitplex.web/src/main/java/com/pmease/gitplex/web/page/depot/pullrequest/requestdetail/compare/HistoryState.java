package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.compare;

import java.io.Serializable;

class HistoryState implements Serializable {

	private static final long serialVersionUID = 1L;

	public String oldRev;
	
	public String newRev;
	
	public String path;
	
	public String comparePath;
	
	public Long commentId;
}
