package com.turbodev.server.web.websocket;

import org.eclipse.jgit.lib.ObjectId;

import com.turbodev.server.web.websocket.WebSocketRegion;

public class CommitIndexedRegion implements WebSocketRegion {

	private final ObjectId commitId;

	public CommitIndexedRegion(ObjectId commitId) {
		this.commitId = commitId;
	}
	
	@Override
	public boolean contains(WebSocketRegion region) {
		if (region instanceof CommitIndexedRegion) {
			CommitIndexedRegion commitIndexedRegion = (CommitIndexedRegion) region;  
		    return commitId.equals(commitIndexedRegion.commitId);
		} else {
			return false;
		}
	}
	
}