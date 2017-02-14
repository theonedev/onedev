package com.gitplex.server.web.websocket;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.commons.wicket.websocket.WebSocketRegion;

public class CommitIndexedRegion implements WebSocketRegion {

	private final Long depotId;
	
	private final ObjectId commitId;

	public CommitIndexedRegion(Long depotId, ObjectId commitId) {
		this.depotId = depotId;
		this.commitId = commitId;
	}
	
	@Override
	public boolean contains(WebSocketRegion region) {
		if (region instanceof CommitIndexedRegion) {
			CommitIndexedRegion commitIndexedRegion = (CommitIndexedRegion) region;  
		    return depotId.equals(commitIndexedRegion.depotId) && commitId.equals(commitIndexedRegion.commitId);
		} else {
			return false;
		}
	}
	
}