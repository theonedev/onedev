package com.pmease.gitplex.web.page.repository.file;

import java.io.Serializable;

import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.lang.TokenPosition;

public class HistoryState implements Serializable {

	private static final long serialVersionUID = 1L;

	public BlobIdent file = new BlobIdent();
	
	public TokenPosition tokenPos;
	
	public boolean blame;
	
	public HistoryState() {
	}

	public HistoryState(HistoryState copy) {
		file = new BlobIdent(copy.file);
		tokenPos = copy.tokenPos;
		blame = copy.blame;
	}
	
}